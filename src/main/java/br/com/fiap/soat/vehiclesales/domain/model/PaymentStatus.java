package br.com.fiap.soat.vehiclesales.domain.model;

/** PENDING -> PAID ou PENDING -> CANCELED. PAID e CANCELED são finais. */
public enum PaymentStatus {
    PENDING,
    PAID,
    CANCELED
}
