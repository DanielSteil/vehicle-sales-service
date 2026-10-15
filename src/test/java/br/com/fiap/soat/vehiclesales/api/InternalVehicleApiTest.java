package br.com.fiap.soat.vehiclesales.api;

import br.com.fiap.soat.vehiclesales.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalVehicleApiTest extends IntegrationTest {

    @Test
    void firstSyncCreatesAnAvailableVehicle() throws Exception {
        UUID id = UUID.randomUUID();

        syncVehicle(id, vehicleJson("Toyota", "Corolla", 2022, "Prata", "120000.00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        mockMvc.perform(get("/vehicles/available"))
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].model").value("Corolla"));
    }

    @Test
    void laterSyncUpdatesTheVehicle() throws Exception {
        UUID id = givenAvailableVehicle("Corolla", "120000.00");

        syncVehicle(id, vehicleJson("Toyota", "Corolla Cross", 2023, "Branco", "135000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Corolla Cross"))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.price").value(135000.00));
    }

    @Test
    void reservedVehicleCannotBeChanged() throws Exception {
        UUID id = givenAvailableVehicle("Corolla", "120000.00");
        purchaseAndGetPaymentCode(id);

        syncVehicle(id, vehicleJson("Toyota", "Corolla", 2022, "Prata", "1.00"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value(containsString("RESERVED")));
    }

    @Test
    void requiresTheInternalToken() throws Exception {
        mockMvc.perform(put("/internal/vehicles/{id}", UUID.randomUUID())
                        .contentType(APPLICATION_JSON)
                        .content(vehicleJson("Toyota", "Corolla", 2022, "Prata", "1.00")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Header X-Internal-Token ausente ou inválido"));
    }

    @Test
    void rejectsAWrongInternalToken() throws Exception {
        mockMvc.perform(put("/internal/vehicles/{id}", UUID.randomUUID())
                        .header(INTERNAL_TOKEN_HEADER, "wrong")
                        .contentType(APPLICATION_JSON)
                        .content(vehicleJson("Toyota", "Corolla", 2022, "Prata", "1.00")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatesThePayload() throws Exception {
        syncVehicle(UUID.randomUUID(), """
                {"brand":"","model":"Corolla","year":1500,"color":"Prata","price":-1}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.errors.brand").exists())
                .andExpect(jsonPath("$.errors.year").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }
}
