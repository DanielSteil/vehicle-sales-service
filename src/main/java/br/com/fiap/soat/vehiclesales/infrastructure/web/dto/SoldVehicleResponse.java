package br.com.fiap.soat.vehiclesales.infrastructure.web.dto;

import br.com.fiap.soat.vehiclesales.domain.model.SoldVehicle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** O CPF do comprador sai mascarado porque a listagem é pública. */
public record SoldVehicleResponse(UUID vehicleId, String brand, String model, int year, String color,
                                  BigDecimal price, UUID saleId, LocalDate saleDate, String buyerCpf) {

    public static SoldVehicleResponse from(SoldVehicle sold) {
        return new SoldVehicleResponse(sold.vehicleId(), sold.brand(), sold.model(), sold.year(), sold.color(),
                sold.price(), sold.saleId(), sold.saleDate(), sold.buyerCpf().masked());
    }
}
