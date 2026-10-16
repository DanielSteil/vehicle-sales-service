package br.com.fiap.soat.vehiclesales.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record PurchaseRequest(
        @NotNull UUID vehicleId,
        @Schema(description = "CPF do comprador, com ou sem máscara", example = "529.982.247-25")
        @NotBlank String buyerCpf,
        @Schema(description = "Data da venda; quando omitida, usa a data de hoje", example = "2026-10-15")
        LocalDate saleDate) {
}
