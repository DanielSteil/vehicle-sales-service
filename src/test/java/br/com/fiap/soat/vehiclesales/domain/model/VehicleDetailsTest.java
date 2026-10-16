package br.com.fiap.soat.vehiclesales.domain.model;

import br.com.fiap.soat.vehiclesales.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleDetailsTest {

    @Test
    void acceptsCompleteData() {
        VehicleDetails details = new VehicleDetails("Toyota", "Corolla", 2022, "Prata", new BigDecimal("120000.00"));

        assertThat(details.brand()).isEqualTo("Toyota");
    }

    @Test
    void requiresBrandModelAndColor() {
        assertThatThrownBy(() -> new VehicleDetails(" ", "Corolla", 2022, "Prata", BigDecimal.TEN))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("marca");
        assertThatThrownBy(() -> new VehicleDetails("Toyota", null, 2022, "Prata", BigDecimal.TEN))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("modelo");
        assertThatThrownBy(() -> new VehicleDetails("Toyota", "Corolla", 2022, "", BigDecimal.TEN))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("cor");
    }

    @Test
    void rejectsInvalidYear() {
        assertThatThrownBy(() -> new VehicleDetails("Toyota", "Corolla", 0, "Prata", BigDecimal.TEN))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsMissingOrNonPositivePrice() {
        assertThatThrownBy(() -> new VehicleDetails("Toyota", "Corolla", 2022, "Prata", null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new VehicleDetails("Toyota", "Corolla", 2022, "Prata", BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class);
    }
}
