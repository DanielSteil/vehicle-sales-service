package br.com.fiap.soat.vehiclesales.application.usecase;

import br.com.fiap.soat.vehiclesales.application.port.SaleRepository;
import br.com.fiap.soat.vehiclesales.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclesales.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.exception.SaleNotFoundException;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotFoundException;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;

import java.util.UUID;

/**
 * Trata a notificação do processador de pagamento.
 * Pago: a venda é efetivada e o veículo passa a vendido.
 * Cancelado: a venda é cancelada e o veículo volta a ficar à venda.
 * Notificações repetidas são ignoradas, porque processadores costumam reenviar.
 */
public class ProcessPaymentNotificationUseCase {

    private final SaleRepository sales;
    private final VehicleRepository vehicles;
    private final UnitOfWork unitOfWork;

    public ProcessPaymentNotificationUseCase(SaleRepository sales, VehicleRepository vehicles,
                                             UnitOfWork unitOfWork) {
        this.sales = sales;
        this.vehicles = vehicles;
        this.unitOfWork = unitOfWork;
    }

    public Sale execute(UUID paymentCode, PaymentOutcome outcome) {
        return unitOfWork.execute(() -> {
            Sale sale = sales.findByPaymentCode(paymentCode)
                    .orElseThrow(() -> new SaleNotFoundException(
                            "Nenhuma venda encontrada para o código de pagamento " + paymentCode));

            boolean changed = outcome == PaymentOutcome.PAID ? sale.confirmPayment() : sale.cancelPayment();
            if (!changed) {
                return sale;
            }

            Vehicle vehicle = vehicles.findById(sale.getVehicleId())
                    .orElseThrow(() -> new VehicleNotFoundException(sale.getVehicleId()));
            if (outcome == PaymentOutcome.PAID) {
                vehicle.markAsSold();
            } else {
                vehicle.release();
            }
            vehicles.save(vehicle);
            return sales.save(sale);
        });
    }
}
