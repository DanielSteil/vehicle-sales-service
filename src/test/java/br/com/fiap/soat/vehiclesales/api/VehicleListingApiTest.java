package br.com.fiap.soat.vehiclesales.api;

import br.com.fiap.soat.vehiclesales.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VehicleListingApiTest extends IntegrationTest {

    @Test
    void listsAvailableVehiclesFromCheapestToMostExpensive() throws Exception {
        givenAvailableVehicle("Hilux", "250000.00");
        givenAvailableVehicle("Etios", "45000.50");
        givenAvailableVehicle("Corolla", "120000.00");

        mockMvc.perform(get("/vehicles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].model").value("Etios"))
                .andExpect(jsonPath("$.content[0].price").value(45000.50))
                .andExpect(jsonPath("$.content[1].model").value("Corolla"))
                .andExpect(jsonPath("$.content[2].model").value("Hilux"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void paginatesTheAvailableList() throws Exception {
        givenAvailableVehicle("A", "10000.00");
        givenAvailableVehicle("B", "20000.00");
        givenAvailableVehicle("C", "30000.00");

        mockMvc.perform(get("/vehicles/available").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].model").value("C"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void reservedAndSoldVehiclesAreNotListedAsAvailable() throws Exception {
        UUID available = givenAvailableVehicle("Disponível", "50000.00");
        purchaseAndGetPaymentCode(givenAvailableVehicle("Reservado", "60000.00"));
        givenSoldVehicle("Vendido", "70000.00");

        mockMvc.perform(get("/vehicles/available"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(available.toString()));
    }

    @Test
    void listsSoldVehiclesFromCheapestToMostExpensiveWithMaskedCpf() throws Exception {
        givenSoldVehicle("Caro", "99000.00");
        givenSoldVehicle("Barato", "31000.00");
        purchaseAndGetPaymentCode(givenAvailableVehicle("Pagamento pendente", "10.00"));
        givenAvailableVehicle("À venda", "5.00");

        mockMvc.perform(get("/vehicles/sold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].model").value("Barato"))
                .andExpect(jsonPath("$.content[0].price").value(31000.00))
                .andExpect(jsonPath("$.content[0].buyerCpf").value("***.982.247-**"))
                .andExpect(jsonPath("$.content[0].saleId").exists())
                .andExpect(jsonPath("$.content[0].saleDate").exists())
                .andExpect(jsonPath("$.content[1].model").value("Caro"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void rejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/vehicles/available").param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/vehicles/sold").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }
}
