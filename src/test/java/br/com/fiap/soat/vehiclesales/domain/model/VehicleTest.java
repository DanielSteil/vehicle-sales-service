package br.com.fiap.soat.vehiclesales.domain.model;

import br.com.fiap.soat.vehiclesales.domain.exception.InvalidStatusTransitionException;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotAvailableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleTest {

    private static final VehicleDetails DETAILS =
            new VehicleDetails("Honda", "Civic", 2021, "Preto", new BigDecimal("98000.00"));

    @Test
    void startsAvailable() {
        Vehicle vehicle = Vehicle.available(UUID.randomUUID(), DETAILS);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(vehicle.getVersion()).isNull();
    }

    @Test
    void updatesDetailsWhileAvailable() {
        Vehicle vehicle = Vehicle.available(UUID.randomUUID(), DETAILS);
        VehicleDetails newDetails = new VehicleDetails("Honda", "Civic", 2021, "Branco", new BigDecimal("95000.00"));

        vehicle.updateDetails(newDetails);

        assertThat(vehicle.getDetails()).isEqualTo(newDetails);
    }

    @Test
    void refusesDetailsChangeOnceReserved() {
        Vehicle vehicle = Vehicle.available(UUID.randomUUID(), DETAILS);
        vehicle.reserve();

        assertThatThrownBy(() -> vehicle.updateDetails(DETAILS))
                .isInstanceOf(VehicleNotAvailableException.class)
                .hasMessageContaining("RESERVED");
    }

    @Test
    void cannotBeReservedTwice() {
        Vehicle vehicle = Vehicle.available(UUID.randomUUID(), DETAILS);
        vehicle.reserve();

        assertThatThrownBy(vehicle::reserve).isInstanceOf(VehicleNotAvailableException.class);
    }

    @Test
    void reservedVehicleCanBeSold() {
        Vehicle vehicle = Vehicle.available(UUID.randomUUID(), DETAILS);
        vehicle.reserve();

        vehicle.markAsSold();

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.SOLD);
    }

    @Test
    void reservedVehicleCanBeReleased() {
        Vehicle vehicle = Vehicle.available(UUID.randomUUID(), DETAILS);
        vehicle.reserve();

        vehicle.release();

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void onlyReservedVehiclesCanBeSoldOrReleased() {
        Vehicle vehicle = Vehicle.available(UUID.randomUUID(), DETAILS);

        assertThatThrownBy(vehicle::markAsSold).isInstanceOf(InvalidStatusTransitionException.class);
        assertThatThrownBy(vehicle::release).isInstanceOf(InvalidStatusTransitionException.class);
    }
}
