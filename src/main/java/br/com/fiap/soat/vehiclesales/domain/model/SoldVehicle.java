package br.com.fiap.soat.vehiclesales.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Visão de leitura de um veículo vendido (venda com pagamento confirmado). */
public record SoldVehicle(UUID vehicleId, String brand, String model, int year, String color,
                          BigDecimal price, UUID saleId, LocalDate saleDate, Cpf buyerCpf) {
}
