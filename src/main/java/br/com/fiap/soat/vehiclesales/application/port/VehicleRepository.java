package br.com.fiap.soat.vehiclesales.application.port;

import br.com.fiap.soat.vehiclesales.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclesales.application.pagination.PageResult;
import br.com.fiap.soat.vehiclesales.domain.model.SoldVehicle;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

    Optional<Vehicle> findById(UUID id);

    /** Persiste o veículo; falha se outra transação o alterou desde a leitura (lock otimista). */
    Vehicle save(Vehicle vehicle);

    PageResult<Vehicle> findAvailableOrderedByPrice(PageQuery query);

    PageResult<SoldVehicle> findSoldOrderedByPrice(PageQuery query);
}
