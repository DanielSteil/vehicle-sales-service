package br.com.fiap.soat.vehiclesales.application.port;

import br.com.fiap.soat.vehiclesales.domain.model.Sale;

import java.util.Optional;
import java.util.UUID;

public interface SaleRepository {

    Optional<Sale> findById(UUID id);

    Optional<Sale> findByPaymentCode(UUID paymentCode);

    Sale save(Sale sale);
}
