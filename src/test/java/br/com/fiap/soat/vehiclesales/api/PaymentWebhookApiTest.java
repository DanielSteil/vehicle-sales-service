package br.com.fiap.soat.vehiclesales.api;

import br.com.fiap.soat.vehiclesales.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentWebhookApiTest extends IntegrationTest {

    @Test
    void paidNotificationCompletesTheSale() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");
        String paymentCode = purchaseAndGetPaymentCode(vehicleId);

        notifyPayment(paymentCode, "PAID")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        mockMvc.perform(get("/vehicles/sold"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].vehicleId").value(vehicleId.toString()));
    }

    @Test
    void canceledNotificationPutsTheVehicleBackOnSale() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");
        String paymentCode = purchaseAndGetPaymentCode(vehicleId);

        notifyPayment(paymentCode, "CANCELED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("CANCELED"));

        mockMvc.perform(get("/vehicles/available"))
                .andExpect(jsonPath("$.content[0].id").value(vehicleId.toString()));
        mockMvc.perform(get("/vehicles/sold")).andExpect(jsonPath("$.content", hasSize(0)));
        // o veículo pode ser comprado de novo por outra pessoa
        purchase(vehicleId, "111.444.777-35").andExpect(status().isCreated());
    }

    @Test
    void repeatedNotificationIsAcceptedWithoutSideEffects() throws Exception {
        String paymentCode = purchaseAndGetPaymentCode(givenAvailableVehicle("Corolla", "120000.00"));

        notifyPayment(paymentCode, "PAID").andExpect(status().isOk());
        notifyPayment(paymentCode, "PAID")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        mockMvc.perform(get("/vehicles/sold")).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void contradictoryNotificationIsRejected() throws Exception {
        String paymentCode = purchaseAndGetPaymentCode(givenAvailableVehicle("Corolla", "120000.00"));
        notifyPayment(paymentCode, "PAID").andExpect(status().isOk());

        notifyPayment(paymentCode, "CANCELED")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito com o estado atual"));
    }

    @Test
    void unknownPaymentCodeIsNotFound() throws Exception {
        notifyPayment(UUID.randomUUID().toString(), "PAID").andExpect(status().isNotFound());
    }

    @Test
    void onlyPaidOrCanceledAreAccepted() throws Exception {
        String paymentCode = purchaseAndGetPaymentCode(givenAvailableVehicle("Corolla", "120000.00"));

        notifyPayment(paymentCode, "PENDING").andExpect(status().isBadRequest());
        notifyPayment(paymentCode, "null").andExpect(status().isBadRequest());
    }

    @Test
    void requiresTheWebhookSecret() throws Exception {
        String paymentCode = purchaseAndGetPaymentCode(givenAvailableVehicle("Corolla", "120000.00"));

        mockMvc.perform(post("/webhooks/payments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"paymentCode":"%s","status":"PAID"}
                                """.formatted(paymentCode)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Header X-Webhook-Secret ausente ou inválido"));
    }
}
