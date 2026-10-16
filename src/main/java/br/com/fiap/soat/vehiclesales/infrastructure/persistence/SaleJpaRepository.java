package br.com.fiap.soat.vehiclesales.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SaleJpaRepository extends JpaRepository<SaleJpaEntity, UUID> {

    Optional<SaleJpaEntity> findByPaymentCode(UUID paymentCode);
}
