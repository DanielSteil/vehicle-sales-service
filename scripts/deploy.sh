#!/usr/bin/env bash
# Publica o serviço de venda no Kubernetes. Usado pelo pipeline de CD e para deploy manual.
#
# Variáveis obrigatórias: IMAGE, SALES_DB_PASSWORD, INTERNAL_API_TOKEN, WEBHOOK_SECRET
# Opcional: KUBE_CONTEXT (ex.: minikube)
set -euo pipefail

: "${IMAGE:?defina IMAGE (ex.: ghcr.io/<dono>/vehicle-sales-service:<tag>)}"
: "${SALES_DB_PASSWORD:?defina SALES_DB_PASSWORD}"
: "${INTERNAL_API_TOKEN:?defina INTERNAL_API_TOKEN}"
: "${WEBHOOK_SECRET:?defina WEBHOOK_SECRET}"

NAMESPACE=vehicle-platform
cd "$(dirname "$0")/.."

kube() { kubectl ${KUBE_CONTEXT:+--context "$KUBE_CONTEXT"} "$@"; }
apply_secret() {
  local name=$1
  shift
  kube -n "$NAMESPACE" create secret generic "$name" "$@" --dry-run=client -o yaml | kube apply -f -
}

echo "==> Namespace e segredos"
kube apply -f k8s/namespace.yaml
apply_secret sales-db-credentials \
  --from-literal=username=sales \
  --from-literal=password="$SALES_DB_PASSWORD"
apply_secret vehicle-sales-service-secrets \
  --from-literal=INTERNAL_API_TOKEN="$INTERNAL_API_TOKEN" \
  --from-literal=WEBHOOK_SECRET="$WEBHOOK_SECRET"

echo "==> Manifests (imagem: $IMAGE)"
SECRETS_HASH=$(printf '%s|%s|%s' "$SALES_DB_PASSWORD" "$INTERNAL_API_TOKEN" "$WEBHOOK_SECRET" | sha256sum | cut -c1-16)
kube kustomize k8s \
  | sed -E -e "s#^([[:space:]]*image:[[:space:]]*)vehicle-sales-service\$#\1${IMAGE}#" \
           -e "s#^([[:space:]]*vehicle-platform/secrets-hash:[[:space:]]*).*#\1\"${SECRETS_HASH}\"#" \
  | kube apply -f -

echo "==> Aguardando o rollout"
kube -n "$NAMESPACE" rollout status statefulset/sales-db --timeout=180s
kube -n "$NAMESPACE" rollout status deployment/vehicle-sales-service --timeout=300s
kube -n "$NAMESPACE" get pods -l app.kubernetes.io/part-of=vehicle-platform -o wide
