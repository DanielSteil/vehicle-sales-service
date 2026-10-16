package br.com.fiap.soat.vehiclesales.application.usecase;

import br.com.fiap.soat.vehiclesales.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclesales.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleDetails;

import java.util.UUID;

/**
 * Recebe do serviço principal os dados de um veículo cadastrado ou editado.
 * É idempotente: repetir a chamada com os mesmos dados não muda nada.
 */
public class SyncVehicleUseCase {

    private final VehicleRepository vehicles;
    private final UnitOfWork unitOfWork;

    public SyncVehicleUseCase(VehicleRepository vehicles, UnitOfWork unitOfWork) {
        this.vehicles = vehicles;
        this.unitOfWork = unitOfWork;
    }

    public Result execute(UUID vehicleId, VehicleDetails details) {
        return unitOfWork.execute(() -> vehicles.findById(vehicleId)
                .map(existing -> {
                    existing.updateDetails(details);
                    return new Result(vehicles.save(existing), false);
                })
                .orElseGet(() -> new Result(vehicles.save(Vehicle.available(vehicleId, details)), true)));
    }

    public record Result(Vehicle vehicle, boolean created) {
    }
}
