package br.com.fiap.soat.vehiclesales.infrastructure.web.dto;

import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleDetails;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleResponse(UUID id, String brand, String model, int year, String color,
                              BigDecimal price, VehicleStatus status) {

    public static VehicleResponse from(Vehicle vehicle) {
        VehicleDetails details = vehicle.getDetails();
        return new VehicleResponse(vehicle.getId(), details.brand(), details.model(), details.year(),
                details.color(), details.price(), vehicle.getStatus());
    }
}
