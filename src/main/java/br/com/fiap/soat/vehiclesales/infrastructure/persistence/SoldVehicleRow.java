package br.com.fiap.soat.vehiclesales.infrastructure.persistence;

import br.com.fiap.soat.vehiclesales.domain.model.Cpf;
import br.com.fiap.soat.vehiclesales.domain.model.SoldVehicle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Linha da consulta de vendidos (junção venda paga + veículo). */
public record SoldVehicleRow(UUID vehicleId, String brand, String model, int year, String color,
                             BigDecimal price, UUID saleId, LocalDate saleDate, String buyerCpf) {

    SoldVehicle toDomain() {
        return new SoldVehicle(vehicleId, brand, model, year, color, price, saleId, saleDate, new Cpf(buyerCpf));
    }
}
