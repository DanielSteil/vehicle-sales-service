package br.com.fiap.soat.vehiclesales.application.usecase.fakes;

import br.com.fiap.soat.vehiclesales.application.port.SaleRepository;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemorySaleRepository implements SaleRepository {

    private final Map<UUID, Sale> store = new HashMap<>();

    @Override
    public Optional<Sale> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Sale> findByPaymentCode(UUID paymentCode) {
        return store.values().stream().filter(sale -> sale.getPaymentCode().equals(paymentCode)).findFirst();
    }

    @Override
    public Sale save(Sale sale) {
        store.put(sale.getId(), sale);
        return sale;
    }

    public int count() {
        return store.size();
    }
}
