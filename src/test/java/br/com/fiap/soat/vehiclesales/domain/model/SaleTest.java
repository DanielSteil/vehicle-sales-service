package br.com.fiap.soat.vehiclesales.domain.model;

import br.com.fiap.soat.vehiclesales.domain.exception.InvalidStatusTransitionException;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotAvailableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaleTest {

    private static final Cpf BUYER = new Cpf("52998224725");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    private Vehicle availableVehicle() {
        return Vehicle.available(UUID.randomUUID(),
                new VehicleDetails("Fiat", "Pulse", 2023, "Vermelho", new BigDecimal("105000.00")));
    }

    @Test
    void openingASaleReservesTheVehicleAndCopiesItsPrice() {
        Vehicle vehicle = availableVehicle();

        Sale sale = Sale.open(vehicle, BUYER, TODAY);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RESERVED);
        assertThat(sale.getVehicleId()).isEqualTo(vehicle.getId());
        assertThat(sale.getPrice()).isEqualByComparingTo("105000.00");
        assertThat(sale.getBuyerCpf()).isEqualTo(BUYER);
        assertThat(sale.getSaleDate()).isEqualTo(TODAY);
        assertThat(sale.getPaymentCode()).isNotNull();
        assertThat(sale.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(sale.getVersion()).isNull();
    }

    @Test
    void cannotOpenASaleForAnUnavailableVehicle() {
        Vehicle vehicle = availableVehicle();
        Sale.open(vehicle, BUYER, TODAY);

        assertThatThrownBy(() -> Sale.open(vehicle, BUYER, TODAY))
                .isInstanceOf(VehicleNotAvailableException.class);
    }

    @Test
    void confirmingTwiceIsIgnored() {
        Sale sale = Sale.open(availableVehicle(), BUYER, TODAY);

        assertThat(sale.confirmPayment()).isTrue();
        assertThat(sale.confirmPayment()).isFalse();
        assertThat(sale.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void cancelingTwiceIsIgnored() {
        Sale sale = Sale.open(availableVehicle(), BUYER, TODAY);

        assertThat(sale.cancelPayment()).isTrue();
        assertThat(sale.cancelPayment()).isFalse();
        assertThat(sale.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    void paidSaleCannotBeCanceled() {
        Sale sale = Sale.open(availableVehicle(), BUYER, TODAY);
        sale.confirmPayment();

        assertThatThrownBy(sale::cancelPayment)
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("PAID");
    }

    @Test
    void canceledSaleCannotBePaid() {
        Sale sale = Sale.open(availableVehicle(), BUYER, TODAY);
        sale.cancelPayment();

        assertThatThrownBy(sale::confirmPayment).isInstanceOf(InvalidStatusTransitionException.class);
    }
}
