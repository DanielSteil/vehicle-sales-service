package br.com.fiap.soat.vehiclesales.application.usecase;

import br.com.fiap.soat.vehiclesales.application.port.SaleRepository;
import br.com.fiap.soat.vehiclesales.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclesales.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclesales.domain.exception.BusinessRuleException;
import br.com.fiap.soat.vehiclesales.domain.exception.VehicleNotFoundException;
import br.com.fiap.soat.vehiclesales.domain.model.Cpf;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;
import br.com.fiap.soat.vehiclesales.domain.model.Vehicle;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Inicia a compra: reserva o veículo e gera o código que o processador de pagamento
 * usará para avisar, pelo webhook, se o pagamento foi efetuado ou cancelado.
 */
public class PurchaseVehicleUseCase {

    private final VehicleRepository vehicles;
    private final SaleRepository sales;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public PurchaseVehicleUseCase(VehicleRepository vehicles, SaleRepository sales,
                                  UnitOfWork unitOfWork, Clock clock) {
        this.vehicles = vehicles;
        this.sales = sales;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    public Sale execute(Command command) {
        Cpf buyerCpf = new Cpf(command.buyerCpf());
        LocalDate today = LocalDate.now(clock);
        LocalDate saleDate = command.saleDate() != null ? command.saleDate() : today;
        if (saleDate.isAfter(today)) {
            throw new BusinessRuleException("A data da venda não pode estar no futuro");
        }

        return unitOfWork.execute(() -> {
            Vehicle vehicle = vehicles.findById(command.vehicleId())
                    .orElseThrow(() -> new VehicleNotFoundException(command.vehicleId()));
            Sale sale = Sale.open(vehicle, buyerCpf, saleDate);
            vehicles.save(vehicle);
            return sales.save(sale);
        });
    }

    /** @param saleDate opcional; quando ausente, usa a data de hoje */
    public record Command(UUID vehicleId, String buyerCpf, LocalDate saleDate) {
    }
}
