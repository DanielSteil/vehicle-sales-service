package br.com.fiap.soat.vehiclesales.application.usecase;

import br.com.fiap.soat.vehiclesales.application.port.SaleRepository;
import br.com.fiap.soat.vehiclesales.domain.exception.SaleNotFoundException;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;

import java.util.UUID;

public class GetSaleUseCase {

    private final SaleRepository sales;

    public GetSaleUseCase(SaleRepository sales) {
        this.sales = sales;
    }

    public Sale execute(UUID saleId) {
        return sales.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException("Venda " + saleId + " não encontrada"));
    }
}
