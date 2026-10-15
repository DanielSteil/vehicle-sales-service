package br.com.fiap.soat.vehiclesales.domain.exception;

import br.com.fiap.soat.vehiclesales.domain.model.VehicleStatus;

import java.util.UUID;

public class VehicleNotAvailableException extends ConflictException {

    public VehicleNotAvailableException(UUID vehicleId, VehicleStatus status) {
        super("Veículo " + vehicleId + " não está disponível (status atual: " + status + ")");
    }
}
