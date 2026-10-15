package br.com.fiap.soat.vehiclesales.application.usecase;

import br.com.fiap.soat.vehiclesales.application.usecase.fakes.DirectUnitOfWork;
import br.com.fiap.soat.vehiclesales.application.usecase.fakes.InMemoryVehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotAvailableException;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleDetails;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncVehicleUseCaseTest {

    private final InMemoryVehicleRepository vehicles = new InMemoryVehicleRepository();
    private final SyncVehicleUseCase useCase = new SyncVehicleUseCase(vehicles, new DirectUnitOfWork());

    private static VehicleDetails details(String price) {
        return new VehicleDetails("VW", "Nivus", 2024, "Cinza", new BigDecimal(price));
    }

    @Test
    void createsAvailableVehicleOnFirstSync() {
        UUID id = UUID.randomUUID();

        SyncVehicleUseCase.Result result = useCase.execute(id, details("130000.00"));

        assertThat(result.created()).isTrue();
        assertThat(result.vehicle().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(vehicles.findById(id)).isPresent();
    }

    @Test
    void updatesExistingVehicle() {
        UUID id = UUID.randomUUID();
        useCase.execute(id, details("130000.00"));

        SyncVehicleUseCase.Result result = useCase.execute(id, details("125000.00"));

        assertThat(result.created()).isFalse();
        assertThat(result.vehicle().getDetails().price()).isEqualByComparingTo("125000.00");
    }

    @Test
    void refusesToChangeReservedVehicle() {
        UUID id = UUID.randomUUID();
        useCase.execute(id, details("130000.00")).vehicle().reserve();

        assertThatThrownBy(() -> useCase.execute(id, details("1.00")))
                .isInstanceOf(VehicleNotAvailableException.class);
    }
}
