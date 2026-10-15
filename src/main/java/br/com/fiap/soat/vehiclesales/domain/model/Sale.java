package br.com.fiap.soat.vehiclesales.domain.model;

import br.com.fiap.soat.vehiclesales.domain.exception.InvalidStatusTransitionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Venda de um veículo. O preço é copiado no momento da compra para não mudar
 * caso o cadastro do veículo seja alterado depois.
 */
public class Sale {

    private final UUID id;
    private final UUID vehicleId;
    private final Cpf buyerCpf;
    private final LocalDate saleDate;
    private final BigDecimal price;
    private final UUID paymentCode;
    private PaymentStatus paymentStatus;
    private final Long version;

    public Sale(UUID id, UUID vehicleId, Cpf buyerCpf, LocalDate saleDate, BigDecimal price,
                UUID paymentCode, PaymentStatus paymentStatus, Long version) {
        this.id = Objects.requireNonNull(id, "id");
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId");
        this.buyerCpf = Objects.requireNonNull(buyerCpf, "buyerCpf");
        this.saleDate = Objects.requireNonNull(saleDate, "saleDate");
        this.price = Objects.requireNonNull(price, "price");
        this.paymentCode = Objects.requireNonNull(paymentCode, "paymentCode");
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus");
        this.version = version;
    }

    /** Abre a venda e reserva o veículo até o processador de pagamento responder. */
    public static Sale open(Vehicle vehicle, Cpf buyerCpf, LocalDate saleDate) {
        vehicle.reserve();
        return new Sale(UUID.randomUUID(), vehicle.getId(), buyerCpf, saleDate,
                vehicle.getDetails().price(), UUID.randomUUID(), PaymentStatus.PENDING, null);
    }

    /** @return {@code false} quando a mesma notificação já tinha sido aplicada */
    public boolean confirmPayment() {
        return settle(PaymentStatus.PAID);
    }

    /** @return {@code false} quando a mesma notificação já tinha sido aplicada */
    public boolean cancelPayment() {
        return settle(PaymentStatus.CANCELED);
    }

    private boolean settle(PaymentStatus outcome) {
        if (paymentStatus == outcome) {
            return false;
        }
        if (paymentStatus != PaymentStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Pagamento " + paymentCode + " já está " + paymentStatus + " e não pode virar " + outcome);
        }
        paymentStatus = outcome;
        return true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public Cpf getBuyerCpf() {
        return buyerCpf;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public UUID getPaymentCode() {
        return paymentCode;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Long getVersion() {
        return version;
    }
}
