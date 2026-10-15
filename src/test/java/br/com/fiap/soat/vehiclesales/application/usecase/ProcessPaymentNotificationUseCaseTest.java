package br.com.fiap.soat.vehiclesales.application.usecase;

import br.com.fiap.soat.vehiclesales.application.usecase.fakes.DirectUnitOfWork;
import br.com.fiap.soat.vehiclesales.application.usecase.fakes.InMemorySaleRepository;
import br.com.fiap.soat.vehiclesales.application.usecase.fakes.InMemoryVehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.exception.InvalidStatusTransitionException;
import br.com.fiap.soat.vehiclesales.domain.exception.SaleNotFoundException;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotFoundException;
import br.com.fiap.soat.vehiclesales.domain.model.Cpf;
import br.com.fiap.soat.vehiclesales.domain.model.PaymentStatus;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleDetails;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessPaymentNotificationUseCaseTest {

    private final InMemoryVehicleRepository vehicles = new InMemoryVehicleRepository();
    private final InMemorySaleRepository sales = new InMemorySaleRepository();
    private final ProcessPaymentNotificationUseCase useCase =
            new ProcessPaymentNotificationUseCase(sales, vehicles, new DirectUnitOfWork());

    private Sale givenPendingSale() {
        Vehicle vehicle = vehicles.save(Vehicle.available(UUID.randomUUID(),
                new VehicleDetails("Chevrolet", "Onix", 2023, "Azul", new BigDecimal("85000.00"))));
        Sale sale = Sale.open(vehicle, new Cpf("52998224725"), LocalDate.of(2026, 10, 15));
        vehicles.save(vehicle);
        return sales.save(sale);
    }

    private VehicleStatus vehicleStatusOf(Sale sale) {
        return vehicles.findById(sale.getVehicleId()).orElseThrow().getStatus();
    }

    @Test
    void paidNotificationCompletesTheSale() {
        Sale sale = givenPendingSale();

        Sale result = useCase.execute(sale.getPaymentCode(), PaymentOutcome.PAID);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(vehicleStatusOf(sale)).isEqualTo(VehicleStatus.SOLD);
    }

    @Test
    void canceledNotificationPutsTheVehicleBackOnSale() {
        Sale sale = givenPendingSale();

        Sale result = useCase.execute(sale.getPaymentCode(), PaymentOutcome.CANCELED);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(vehicleStatusOf(sale)).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void repeatedNotificationDoesNothing() {
        Sale sale = givenPendingSale();
        useCase.execute(sale.getPaymentCode(), PaymentOutcome.PAID);
        int savesAfterFirst = vehicles.saves();

        Sale result = useCase.execute(sale.getPaymentCode(), PaymentOutcome.PAID);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(vehicles.saves()).isEqualTo(savesAfterFirst);
    }

    @Test
    void contradictoryNotificationIsRejected() {
        Sale sale = givenPendingSale();
        useCase.execute(sale.getPaymentCode(), PaymentOutcome.PAID);

        assertThatThrownBy(() -> useCase.execute(sale.getPaymentCode(), PaymentOutcome.CANCELED))
                .isInstanceOf(InvalidStatusTransitionException.class);
        assertThat(vehicleStatusOf(sale)).isEqualTo(VehicleStatus.SOLD);
    }

    @Test
    void unknownPaymentCodeIsRejected() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), PaymentOutcome.PAID))
                .isInstanceOf(SaleNotFoundException.class);
    }

    @Test
    void saleWhoseVehicleVanishedIsReported() {
        Vehicle ghost = Vehicle.available(UUID.randomUUID(),
                new VehicleDetails("Ford", "Ka", 2019, "Prata", new BigDecimal("40000.00")));
        Sale sale = sales.save(Sale.open(ghost, new Cpf("52998224725"), LocalDate.of(2026, 10, 15)));

        assertThatThrownBy(() -> useCase.execute(sale.getPaymentCode(), PaymentOutcome.PAID))
                .isInstanceOf(VehicleNotFoundException.class);
    }
}
