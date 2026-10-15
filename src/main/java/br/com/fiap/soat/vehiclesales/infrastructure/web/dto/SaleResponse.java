package br.com.fiap.soat.vehiclesales.infrastructure.web.dto;

import br.com.fiap.soat.vehiclesales.domain.model.PaymentStatus;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SaleResponse(UUID id, UUID vehicleId, String buyerCpf, LocalDate saleDate, BigDecimal price,
                           UUID paymentCode, PaymentStatus paymentStatus) {

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(sale.getId(), sale.getVehicleId(), sale.getBuyerCpf().masked(), sale.getSaleDate(),
                sale.getPrice(), sale.getPaymentCode(), sale.getPaymentStatus());
    }
}
