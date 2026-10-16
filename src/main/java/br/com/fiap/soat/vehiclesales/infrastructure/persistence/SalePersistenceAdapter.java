package br.com.fiap.soat.vehiclesales.infrastructure.persistence;

import br.com.fiap.soat.vehiclesales.application.port.SaleRepository;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class SalePersistenceAdapter implements SaleRepository {

    private final SaleJpaRepository jpa;

    public SalePersistenceAdapter(SaleJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Sale> findById(UUID id) {
        return jpa.findById(id).map(SaleJpaEntity::toDomain);
    }

    @Override
    public Optional<Sale> findByPaymentCode(UUID paymentCode) {
        return jpa.findByPaymentCode(paymentCode).map(SaleJpaEntity::toDomain);
    }

    @Override
    public Sale save(Sale sale) {
        SaleJpaEntity entity = sale.getVersion() == null
                ? new SaleJpaEntity(sale)
                : jpa.findById(sale.getId())
                        .filter(found -> Objects.equals(found.getVersion(), sale.getVersion()))
                        .orElseThrow(() -> new ObjectOptimisticLockingFailureException(
                                SaleJpaEntity.class, sale.getId()));
        entity.apply(sale);
        return jpa.saveAndFlush(entity).toDomain();
    }
}
