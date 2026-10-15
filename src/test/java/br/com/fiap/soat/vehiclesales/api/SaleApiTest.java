package br.com.fiap.soat.vehiclesales.api;

import br.com.fiap.soat.vehiclesales.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SaleApiTest extends IntegrationTest {

    @Test
    void purchaseReservesTheVehicleAndReturnsThePaymentCode() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");
        String today = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();

        MvcResult result = purchase(vehicleId, "529.982.247-25")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleId").value(vehicleId.toString()))
                .andExpect(jsonPath("$.buyerCpf").value("***.982.247-**"))
                .andExpect(jsonPath("$.saleDate").value(today))
                .andExpect(jsonPath("$.price").value(120000.00))
                .andExpect(jsonPath("$.paymentCode").exists())
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andReturn();
        String saleId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        assertThat(result.getResponse().getHeader("Location")).endsWith("/sales/" + saleId);
        mockMvc.perform(get("/vehicles/available")).andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void keepsTheInformedSaleDate() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");

        mockMvc.perform(post("/sales").contentType(APPLICATION_JSON).content("""
                        {"vehicleId":"%s","buyerCpf":"%s","saleDate":"2026-01-10"}
                        """.formatted(vehicleId, VALID_CPF)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleDate").value("2026-01-10"));
    }

    @Test
    void rejectsSaleDateInTheFuture() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");

        mockMvc.perform(post("/sales").contentType(APPLICATION_JSON).content("""
                        {"vehicleId":"%s","buyerCpf":"%s","saleDate":"2999-01-01"}
                        """.formatted(vehicleId, VALID_CPF)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A data da venda não pode estar no futuro"));
    }

    @Test
    void rejectsInvalidCpf() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");

        purchase(vehicleId, "111.111.111-11")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("CPF inválido"));
    }

    @Test
    void rejectsUnknownVehicle() throws Exception {
        purchase(UUID.randomUUID(), VALID_CPF)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    void rejectsVehicleThatIsAlreadyReserved() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");
        purchaseAndGetPaymentCode(vehicleId);

        purchase(vehicleId, VALID_CPF).andExpect(status().isConflict());
    }

    @Test
    void rejectsIncompleteRequest() throws Exception {
        mockMvc.perform(post("/sales").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vehicleId").exists())
                .andExpect(jsonPath("$.errors.buyerCpf").exists());
    }

    @Test
    void onlyOneOfTwoSimultaneousBuyersGetsTheVehicle() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> buyer = () -> {
            start.await();
            return purchase(vehicleId, VALID_CPF).andReturn().getResponse().getStatus();
        };

        List<Integer> statuses = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<Integer>> futures = List.of(executor.submit(buyer), executor.submit(buyer));
            start.countDown();
            for (Future<Integer> future : futures) {
                statuses.add(future.get());
            }
        }

        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    }

    @Test
    void returnsTheSale() throws Exception {
        UUID vehicleId = givenAvailableVehicle("Corolla", "120000.00");
        String location = purchase(vehicleId, VALID_CPF).andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.vehicleId").value(vehicleId.toString()))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"));
    }

    @Test
    void unknownSaleIsNotFound() throws Exception {
        mockMvc.perform(get("/sales/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", endsWith("não encontrada")));
    }
}
