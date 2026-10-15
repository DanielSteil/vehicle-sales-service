package br.com.fiap.soat.vehiclesales.infrastructure.persistence;

import br.com.fiap.soat.vehiclesales.IntegrationTest;
import br.com.fiap.soat.vehiclesales.application.port.SaleRepository;
import br.com.fiap.soat.vehiclesales.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclesales.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.model.Cpf;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Garante que duas transações que leram a mesma versão não sobrescrevem uma à outra. */
class OptimisticLockingTest extends IntegrationTest {

    @Autowired
    private VehicleRepository vehicles;

    @Autowired
    private SaleRepository sales;

    @Autowired
    private UnitOfWork unitOfWork;

    private Vehicle persistedVehicle() {
        Vehicle vehicle = Vehicle.available(UUID.randomUUID(),
                new VehicleDetails("Renault", "Kwid", 2024, "Laranja", new BigDecimal("72000.00")));
        return unitOfWork.execute(() -> vehicles.save(vehicle));
    }

    @Test
    void savingAStaleVehicleFails() {
        UUID id = persistedVehicle().getId();
        Vehicle first = vehicles.findById(id).orElseThrow();
        Vehicle stale = vehicles.findById(id).orElseThrow();

        first.reserve();
        Vehicle saved = unitOfWork.execute(() -> vehicles.save(first));
        stale.reserve();

        assertThat(saved.getVersion()).isGreaterThan(stale.getVersion());
        assertThatThrownBy(() -> unitOfWork.execute(() -> vehicles.save(stale)))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void savingAStaleSaleFails() {
        Vehicle vehicle = persistedVehicle();
        Sale sale = unitOfWork.execute(() -> {
            Sale opened = Sale.open(vehicle, new Cpf("52998224725"), LocalDate.of(2026, 10, 15));
            vehicles.save(vehicle);
            return sales.save(opened);
        });
        Sale first = sales.findById(sale.getId()).orElseThrow();
        Sale stale = sales.findById(sale.getId()).orElseThrow();

        first.confirmPayment();
        unitOfWork.execute(() -> sales.save(first));
        stale.cancelPayment();

        assertThatThrownBy(() -> unitOfWork.execute(() -> sales.save(stale)))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}
