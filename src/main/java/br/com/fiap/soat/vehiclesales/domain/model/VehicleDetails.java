package br.com.fiap.soat.vehiclesales.domain.model;

import br.com.fiap.soat.vehiclesales.domain.exception.BusinessRuleException;

import java.math.BigDecimal;

/** Dados cadastrais do veículo, cuja fonte da verdade é o serviço principal. */
public record VehicleDetails(String brand, String model, int year, String color, BigDecimal price) {

    public VehicleDetails {
        requireText(brand, "marca");
        requireText(model, "modelo");
        requireText(color, "cor");
        if (year <= 0) {
            throw new BusinessRuleException("Ano do veículo inválido");
        }
        if (price == null || price.signum() <= 0) {
            throw new BusinessRuleException("O preço do veículo deve ser maior que zero");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("O campo " + field + " é obrigatório");
        }
    }
}
