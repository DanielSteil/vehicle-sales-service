package br.com.fiap.soat.vehiclesales.infrastructure.persistence;

import br.com.fiap.soat.vehiclesales.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclesales.application.pagination.PageResult;
import br.com.fiap.soat.vehiclesales.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.model.PaymentStatus;
import br.com.fiap.soat.vehiclesales.domain.model.SoldVehicle;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class VehiclePersistenceAdapter implements VehicleRepository {

    private static final Sort CHEAPEST_FIRST = Sort.by("price").ascending().and(Sort.by("id"));

    private final VehicleJpaRepository jpa;

    public VehiclePersistenceAdapter(VehicleJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpa.findById(id).map(VehicleJpaEntity::toDomain);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleJpaEntity entity = vehicle.getVersion() == null
                ? new VehicleJpaEntity(vehicle.getId())
                : jpa.findById(vehicle.getId())
                        .filter(found -> Objects.equals(found.getVersion(), vehicle.getVersion()))
                        .orElseThrow(() -> new ObjectOptimisticLockingFailureException(
                                VehicleJpaEntity.class, vehicle.getId()));
        entity.apply(vehicle);
        return jpa.saveAndFlush(entity).toDomain();
    }

    @Override
    public PageResult<Vehicle> findAvailableOrderedByPrice(PageQuery query) {
        Page<VehicleJpaEntity> page = jpa.findByStatus(VehicleStatus.AVAILABLE,
                PageRequest.of(query.page(), query.size(), CHEAPEST_FIRST));
        return toResult(page, query).map(VehicleJpaEntity::toDomain);
    }

    @Override
    public PageResult<SoldVehicle> findSoldOrderedByPrice(PageQuery query) {
        Page<SoldVehicleRow> page = jpa.findSales(PaymentStatus.PAID, PageRequest.of(query.page(), query.size()));
        return toResult(page, query).map(SoldVehicleRow::toDomain);
    }

    private static <T> PageResult<T> toResult(Page<T> page, PageQuery query) {
        return new PageResult<>(page.getContent(), query.page(), query.size(), page.getTotalElements());
    }
}
