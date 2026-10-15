package br.com.fiap.soat.vehiclesales.infrastructure.persistence;

import br.com.fiap.soat.vehiclesales.domain.model.PaymentStatus;
import br.com.fiap.soat.vehiclesales.domain.model.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, UUID> {

    Page<VehicleJpaEntity> findByStatus(VehicleStatus status, Pageable pageable);

    @Query(value = """
            select new br.com.fiap.soat.vehiclesales.infrastructure.persistence.SoldVehicleRow(
                v.id, v.brand, v.model, v.year, v.color, s.price, s.id, s.saleDate, s.buyerCpf)
            from SaleJpaEntity s join VehicleJpaEntity v on v.id = s.vehicleId
            where s.paymentStatus = :status
            order by s.price asc, s.id asc
            """,
            countQuery = "select count(s) from SaleJpaEntity s where s.paymentStatus = :status")
    Page<SoldVehicleRow> findSales(@Param("status") PaymentStatus status, Pageable pageable);
}
