package br.com.fiap.soat.vehiclesales.application.usecase;

import br.com.fiap.soat.vehiclesales.application.usecase.fakes.DirectUnitOfWork;
import br.com.fiap.soat.vehiclesales.application.usecase.fakes.InMemorySaleRepository;
import br.com.fiap.soat.vehiclesales.application.usecase.fakes.InMemoryVehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.exception.BusinessRuleException;
import br.com.fiap.soat.vehiclesales.domain.exception.InvalidCpfException;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotAvailableException;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotFoundException;
import br.com.fiap.soat.vehiclesales.domain.model.PaymentStatus;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleDetails;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseVehicleUseCaseTest {

    private static final String CPF = "529.982.247-25";
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-10-15T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    private final InMemoryVehicleRepository vehicles = new InMemoryVehicleRepository();
    private final InMemorySaleRepository sales = new InMemorySaleRepository();
    private final PurchaseVehicleUseCase useCase =
            new PurchaseVehicleUseCase(vehicles, sales, new DirectUnitOfWork(), CLOCK);

    private Vehicle givenAvailableVehicle() {
        return vehicles.save(Vehicle.available(UUID.randomUUID(),
                new VehicleDetails("Jeep", "Compass", 2022, "Branco", new BigDecimal("150000.00"))));
    }

    @Test
    void reservesVehicleAndOpensPendingSale() {
        Vehicle vehicle = givenAvailableVehicle();

        Sale sale = useCase.execute(new PurchaseVehicleUseCase.Command(vehicle.getId(), CPF, null));

        assertThat(sale.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(sale.getSaleDate()).isEqualTo(TODAY);
        assertThat(sale.getBuyerCpf().value()).isEqualTo("52998224725");
        assertThat(vehicles.findById(vehicle.getId()).orElseThrow().getStatus()).isEqualTo(VehicleStatus.RESERVED);
        assertThat(sales.findByPaymentCode(sale.getPaymentCode())).isPresent();
    }

    @Test
    void keepsInformedPastSaleDate() {
        Vehicle vehicle = givenAvailableVehicle();

        Sale sale = useCase.execute(new PurchaseVehicleUseCase.Command(vehicle.getId(), CPF, TODAY.minusDays(3)));

        assertThat(sale.getSaleDate()).isEqualTo(TODAY.minusDays(3));
    }

    @Test
    void rejectsFutureSaleDateWithoutTouchingTheVehicle() {
        Vehicle vehicle = givenAvailableVehicle();
        int savesBefore = vehicles.saves();

        assertThatThrownBy(() -> useCase.execute(
                new PurchaseVehicleUseCase.Command(vehicle.getId(), CPF, TODAY.plusDays(1))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("futuro");
        assertThat(vehicles.saves()).isEqualTo(savesBefore);
        assertThat(sales.count()).isZero();
    }

    @Test
    void rejectsInvalidCpf() {
        Vehicle vehicle = givenAvailableVehicle();

        assertThatThrownBy(() -> useCase.execute(
                new PurchaseVehicleUseCase.Command(vehicle.getId(), "123.456.789-00", null)))
                .isInstanceOf(InvalidCpfException.class);
    }

    @Test
    void failsForUnknownVehicle() {
        assertThatThrownBy(() -> useCase.execute(new PurchaseVehicleUseCase.Command(UUID.randomUUID(), CPF, null)))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    @Test
    void failsForAlreadyReservedVehicle() {
        Vehicle vehicle = givenAvailableVehicle();
        useCase.execute(new PurchaseVehicleUseCase.Command(vehicle.getId(), CPF, null));

        assertThatThrownBy(() -> useCase.execute(new PurchaseVehicleUseCase.Command(vehicle.getId(), CPF, null)))
                .isInstanceOf(VehicleNotAvailableException.class);
        assertThat(sales.count()).isEqualTo(1);
    }
}
