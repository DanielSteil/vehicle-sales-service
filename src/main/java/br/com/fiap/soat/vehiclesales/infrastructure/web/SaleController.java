package br.com.fiap.soat.vehiclesales.infrastructure.web;

import br.com.fiap.soat.vehiclesales.application.usecase.GetSaleUseCase;
import br.com.fiap.soat.vehiclesales.application.usecase.PurchaseVehicleUseCase;
import br.com.fiap.soat.vehiclesales.domain.model.Sale;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.PurchaseRequest;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.SaleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/sales")
@Tag(name = "Vendas", description = "Compra de veículos")
public class SaleController {

    private final PurchaseVehicleUseCase purchaseVehicle;
    private final GetSaleUseCase getSale;

    public SaleController(PurchaseVehicleUseCase purchaseVehicle, GetSaleUseCase getSale) {
        this.purchaseVehicle = purchaseVehicle;
        this.getSale = getSale;
    }

    @PostMapping
    @Operation(summary = "Efetua a compra: reserva o veículo e devolve o código de pagamento")
    public ResponseEntity<SaleResponse> purchase(@Valid @RequestBody PurchaseRequest request) {
        Sale sale = purchaseVehicle.execute(new PurchaseVehicleUseCase.Command(
                request.vehicleId(), request.buyerCpf(), request.saleDate()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(sale.getId()).toUri();
        return ResponseEntity.created(location).body(SaleResponse.from(sale));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma venda e o status do pagamento")
    public SaleResponse get(@PathVariable UUID id) {
        return SaleResponse.from(getSale.execute(id));
    }
}
