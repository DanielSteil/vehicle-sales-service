package br.com.fiap.soat.vehiclesales.application.usecase;

/** Resultados que o processador de pagamento pode informar pelo webhook. */
public enum PaymentOutcome {
    PAID,
    CANCELED
}
