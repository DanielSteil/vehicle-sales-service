package br.com.fiap.soat.vehiclesales.application.usecase;

import br.com.fiap.soat.vehiclesales.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclesales.application.pagination.PageResult;
import br.com.fiap.soat.vehiclesales.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.model.SoldVehicle;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;

/** Listagens públicas, ambas ordenadas do mais barato para o mais caro. */
public class ListVehiclesUseCase {

    private final VehicleRepository vehicles;

    public ListVehiclesUseCase(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    public PageResult<Vehicle> available(PageQuery query) {
        return vehicles.findAvailableOrderedByPrice(query);
    }

    public PageResult<SoldVehicle> sold(PageQuery query) {
        return vehicles.findSoldOrderedByPrice(query);
    }
}
