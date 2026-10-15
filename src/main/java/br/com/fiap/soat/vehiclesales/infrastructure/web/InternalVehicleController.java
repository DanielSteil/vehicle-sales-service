package br.com.fiap.soat.vehiclesales.infrastructure.web;

import br.com.fiap.soat.vehiclesales.application.usecase.SyncVehicleUseCase;
import br.com.fiap.soat.vehiclesales.infrastructure.config.OpenApiConfig;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.VehicleResponse;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.VehicleSyncRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/vehicles")
@Tag(name = "Integração interna", description = "Chamado pelo serviço principal a cada cadastro ou edição de veículo")
@SecurityRequirement(name = OpenApiConfig.INTERNAL_TOKEN_SCHEME)
public class InternalVehicleController {

    private final SyncVehicleUseCase syncVehicle;

    public InternalVehicleController(SyncVehicleUseCase syncVehicle) {
        this.syncVehicle = syncVehicle;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cria ou atualiza a cópia local do veículo (409 se estiver reservado ou vendido)")
    public ResponseEntity<VehicleResponse> sync(@PathVariable UUID id, @Valid @RequestBody VehicleSyncRequest request) {
        SyncVehicleUseCase.Result result = syncVehicle.execute(id, request.toDetails());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(VehicleResponse.from(result.vehicle()));
    }
}
