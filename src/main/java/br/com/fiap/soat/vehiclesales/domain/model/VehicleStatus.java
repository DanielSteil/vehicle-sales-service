package br.com.fiap.soat.vehiclesales.domain.model;

/**
 * Ciclo de vida do veículo no serviço de venda.
 * AVAILABLE -> RESERVED (compra iniciada) -> SOLD (pagamento confirmado)
 * RESERVED -> AVAILABLE (pagamento cancelado)
 */
public enum VehicleStatus {
    AVAILABLE,
    RESERVED,
    SOLD
}
