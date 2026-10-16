package br.com.fiap.soat.vehiclesales;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicação inteira contra um PostgreSQL real em contêiner.
 * O contêiner é compartilhado por todas as classes de teste (padrão singleton).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    protected static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    protected static final String INTERNAL_TOKEN = "test-internal-token";
    protected static final String WEBHOOK_SECRET_HEADER = "X-Webhook-Secret";
    protected static final String WEBHOOK_SECRET = "test-webhook-secret";
    protected static final String VALID_CPF = "52998224725";

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE sales, vehicles");
    }

    protected static String vehicleJson(String brand, String model, int year, String color, String price) {
        return """
                {"brand":"%s","model":"%s","year":%d,"color":"%s","price":%s}
                """.formatted(brand, model, year, color, price);
    }

    protected ResultActions syncVehicle(UUID id, String json) throws Exception {
        return mockMvc.perform(put("/internal/vehicles/{id}", id)
                .header(INTERNAL_TOKEN_HEADER, INTERNAL_TOKEN)
                .contentType(APPLICATION_JSON)
                .content(json));
    }

    protected UUID givenAvailableVehicle(String model, String price) throws Exception {
        UUID id = UUID.randomUUID();
        syncVehicle(id, vehicleJson("Toyota", model, 2022, "Prata", price)).andExpect(status().isCreated());
        return id;
    }

    protected ResultActions purchase(UUID vehicleId, String cpf) throws Exception {
        return mockMvc.perform(post("/sales")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"vehicleId":"%s","buyerCpf":"%s"}
                        """.formatted(vehicleId, cpf)));
    }

    protected String purchaseAndGetPaymentCode(UUID vehicleId) throws Exception {
        String body = purchase(vehicleId, VALID_CPF)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.paymentCode");
    }

    protected ResultActions notifyPayment(String paymentCode, String outcome) throws Exception {
        return mockMvc.perform(post("/webhooks/payments")
                .header(WEBHOOK_SECRET_HEADER, WEBHOOK_SECRET)
                .contentType(APPLICATION_JSON)
                .content("""
                        {"paymentCode":"%s","status":"%s"}
                        """.formatted(paymentCode, outcome)));
    }

    protected UUID givenSoldVehicle(String model, String price) throws Exception {
        UUID id = givenAvailableVehicle(model, price);
        notifyPayment(purchaseAndGetPaymentCode(id), "PAID").andExpect(status().isOk());
        return id;
    }
}
