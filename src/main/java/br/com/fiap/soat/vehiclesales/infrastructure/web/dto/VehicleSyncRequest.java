package br.com.fiap.soat.vehiclesales.infrastructure.web.dto;

import br.com.fiap.soat.vehiclesales.domain.model.VehicleDetails;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record VehicleSyncRequest(
        @NotBlank @Size(max = 60) String brand,
        @NotBlank @Size(max = 60) String model,
        @NotNull @Min(1886) @Max(2100) Integer year,
        @NotBlank @Size(max = 30) String color,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal price) {

    public VehicleDetails toDetails() {
        return new VehicleDetails(brand, model, year, color, price);
    }
}
