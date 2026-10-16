#!/usr/bin/env bash
# Gera carga nas listagens do serviço de venda para demonstrar o HPA escalando os pods.
# Uso: bash scripts/load-test.sh [pods geradores de carga, padrão 4] [duração em segundos, padrão 180]
# Opcional: KUBE_CONTEXT (ex.: minikube)
set -euo pipefail

WORKERS="${1:-4}"
DURATION="${2:-180}"

kube() { kubectl ${KUBE_CONTEXT:+--context "$KUBE_CONTEXT"} -n vehicle-platform "$@"; }
cleanup() { kube delete pod -l app=load-generator --wait=false > /dev/null 2>&1 || true; }
trap cleanup EXIT

for i in $(seq 1 "$WORKERS"); do
  kube run "load-generator-$i" --labels=app=load-generator --restart=Never --image=busybox:1.36 -- \
    sh -c "end=\$((\$(date +%s) + $DURATION)); while [ \$(date +%s) -lt \$end ]; do wget -q -O /dev/null http://vehicle-sales-service/vehicles/available; done"
done

echo "Gerando carga por ${DURATION}s com ${WORKERS} pods. Réplicas do serviço de venda:"
end=$(( $(date +%s) + DURATION + 60 ))
while [ "$(date +%s)" -lt "$end" ]; do
  printf '%s  ' "$(date +%T)"
  kube get hpa vehicle-sales-service --no-headers
  sleep 15
done
