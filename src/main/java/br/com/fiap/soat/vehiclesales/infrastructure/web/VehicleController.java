package br.com.fiap.soat.vehiclesales.infrastructure.web;

import br.com.fiap.soat.vehiclesales.application.pagination.PageQuery;
import br.com.fiap.soat.vehiclesales.application.usecase.ListVehiclesUseCase;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.PageResponse;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.SoldVehicleResponse;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vitrine", description = "Listagens públicas de veículos")
public class VehicleController {

    private final ListVehiclesUseCase listVehicles;

    public VehicleController(ListVehiclesUseCase listVehicles) {
        this.listVehicles = listVehicles;
    }

    @GetMapping("/available")
    @Operation(summary = "Lista os veículos à venda, do mais barato para o mais caro")
    public PageResponse<VehicleResponse> available(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(PageQuery.MAX_SIZE) int size) {
        return PageResponse.from(listVehicles.available(new PageQuery(page, size)), VehicleResponse::from);
    }

    @GetMapping("/sold")
    @Operation(summary = "Lista os veículos vendidos (pagamento confirmado), do mais barato para o mais caro")
    public PageResponse<SoldVehicleResponse> sold(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(PageQuery.MAX_SIZE) int size) {
        return PageResponse.from(listVehicles.sold(new PageQuery(page, size)), SoldVehicleResponse::from);
    }
}
