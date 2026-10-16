package br.com.fiap.soat.vehiclesales.application.usecase.fakes;

import br.com.fiap.soat.vehiclesales.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclesales.application.pagination.PageResult;
import br.com.fiap.soat.vehiclesales.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.model.SoldVehicle;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleStatus;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryVehicleRepository implements VehicleRepository {

    private final Map<UUID, Vehicle> store = new HashMap<>();
    private int saves;

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        saves++;
        store.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public PageResult<Vehicle> findAvailableOrderedByPrice(PageQuery query) {
        List<Vehicle> available = store.values().stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE)
                .sorted(Comparator.comparing(vehicle -> vehicle.getDetails().price()))
                .toList();
        return new PageResult<>(available, query.page(), query.size(), available.size());
    }

    @Override
    public PageResult<SoldVehicle> findSoldOrderedByPrice(PageQuery query) {
        return new PageResult<>(List.of(), query.page(), query.size(), 0);
    }

    public int saves() {
        return saves;
    }
}
