package br.com.fiap.soat.vehiclesales.infrastructure.web.dto;

import br.com.fiap.soat.vehiclesales.application.usecase.PaymentOutcome;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentNotificationRequest(@NotNull UUID paymentCode, @NotNull PaymentOutcome status) {
}
