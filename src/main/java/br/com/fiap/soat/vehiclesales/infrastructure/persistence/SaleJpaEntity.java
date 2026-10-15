package br.com.fiap.soat.vehiclesales.infrastructure.persistence;

import br.com.fiap.soat.vehiclesales.domain.model.Cpf;
import br.com.fiap.soat.vehiclesales.domain.model.PaymentStatus;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sales")
public class SaleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "vehicle_id", nullable = false, updatable = false)
    private UUID vehicleId;

    @Column(name = "buyer_cpf", nullable = false, length = 11, updatable = false)
    private String buyerCpf;

    @Column(name = "sale_date", nullable = false, updatable = false)
    private LocalDate saleDate;

    @Column(nullable = false, precision = 12, scale = 2, updatable = false)
    private BigDecimal price;

    @Column(name = "payment_code", nullable = false, unique = true, updatable = false)
    private UUID paymentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SaleJpaEntity() {
    }

    SaleJpaEntity(Sale sale) {
        this.id = sale.getId();
        this.vehicleId = sale.getVehicleId();
        this.buyerCpf = sale.getBuyerCpf().value();
        this.saleDate = sale.getSaleDate();
        this.price = sale.getPrice();
        this.paymentCode = sale.getPaymentCode();
    }

    void apply(Sale sale) {
        this.paymentStatus = sale.getPaymentStatus();
    }

    Sale toDomain() {
        return new Sale(id, vehicleId, new Cpf(buyerCpf), saleDate, price, paymentCode, paymentStatus, version);
    }

    Long getVersion() {
        return version;
    }
}
