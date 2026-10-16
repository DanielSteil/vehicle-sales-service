package br.com.fiap.soat.vehiclesales.infrastructure.config;

import br.com.fiap.soat.vehiclesales.application.port.SaleRepository;
import br.com.fiap.soat.vehiclesales.application.port.UnitOfWork;
import br.com.fiap.soat.vehiclesales.application.port.VehicleRepository;
import br.com.fiap.soat.vehiclesales.application.usecase.GetSaleUseCase;
import br.com.fiap.soat.vehiclesales.application.usecase.ListVehiclesUseCase;
import br.com.fiap.soat.vehiclesales.application.usecase.ProcessPaymentNotificationUseCase;
import br.com.fiap.soat.vehiclesales.application.usecase.PurchaseVehicleUseCase;
import br.com.fiap.soat.vehiclesales.application.usecase.SyncVehicleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Os casos de uso não conhecem o Spring; é aqui que eles são montados. */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    SyncVehicleUseCase syncVehicleUseCase(VehicleRepository vehicles, UnitOfWork unitOfWork) {
        return new SyncVehicleUseCase(vehicles, unitOfWork);
    }

    @Bean
    ListVehiclesUseCase listVehiclesUseCase(VehicleRepository vehicles) {
        return new ListVehiclesUseCase(vehicles);
    }

    @Bean
    PurchaseVehicleUseCase purchaseVehicleUseCase(VehicleRepository vehicles, SaleRepository sales,
                                                  UnitOfWork unitOfWork, Clock clock) {
        return new PurchaseVehicleUseCase(vehicles, sales, unitOfWork, clock);
    }

    @Bean
    ProcessPaymentNotificationUseCase processPaymentNotificationUseCase(SaleRepository sales,
                                                                        VehicleRepository vehicles,
                                                                        UnitOfWork unitOfWork) {
        return new ProcessPaymentNotificationUseCase(sales, vehicles, unitOfWork);
    }

    @Bean
    GetSaleUseCase getSaleUseCase(SaleRepository sales) {
        return new GetSaleUseCase(sales);
    }
}
