package br.com.fiap.soat.vehiclesales.domain.model;

import br.com.fiap.soat.vehiclesales.domain.exception.InvalidStatusTransitionException;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotAvailableException;

import java.util.Objects;
import java.util.UUID;

/**
 * Cópia local do veículo cadastrado no serviço principal, acrescida do status de venda,
 * do qual este serviço é a fonte da verdade.
 */
public class Vehicle {

    private final UUID id;
    private VehicleDetails details;
    private VehicleStatus status;
    private final Long version;

    public Vehicle(UUID id, VehicleDetails details, VehicleStatus status, Long version) {
        this.id = Objects.requireNonNull(id, "id");
        this.details = Objects.requireNonNull(details, "details");
        this.status = Objects.requireNonNull(status, "status");
        this.version = version;
    }

    public static Vehicle available(UUID id, VehicleDetails details) {
        return new Vehicle(id, details, VehicleStatus.AVAILABLE, null);
    }

    /** Dados cadastrais só podem mudar enquanto o veículo está à venda. */
    public void updateDetails(VehicleDetails newDetails) {
        requireAvailable();
        this.details = Objects.requireNonNull(newDetails, "details");
    }

    public void reserve() {
        requireAvailable();
        status = VehicleStatus.RESERVED;
    }

    public void markAsSold() {
        transition(VehicleStatus.RESERVED, VehicleStatus.SOLD);
    }

    public void release() {
        transition(VehicleStatus.RESERVED, VehicleStatus.AVAILABLE);
    }

    private void requireAvailable() {
        if (status != VehicleStatus.AVAILABLE) {
            throw new VehicleNotAvailableException(id, status);
        }
    }

    private void transition(VehicleStatus expected, VehicleStatus target) {
        if (status != expected) {
            throw new InvalidStatusTransitionException(
                    "Veículo " + id + " não pode passar de " + status + " para " + target);
        }
        status = target;
    }

    public UUID getId() {
        return id;
    }

    public VehicleDetails getDetails() {
        return details;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }
}
