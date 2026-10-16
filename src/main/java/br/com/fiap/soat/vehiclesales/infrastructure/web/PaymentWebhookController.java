package br.com.fiap.soat.vehiclesales.infrastructure.web;

import br.com.fiap.soat.vehiclesales.application.usecase.ProcessPaymentNotificationUseCase;
import br.com.fiap.soat.vehiclesales.infrastructure.config.OpenApiConfig;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.PaymentNotificationRequest;
import br.com.fiap.soat.vehiclesales.infrastructure.web.dto.SaleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/payments")
@Tag(name = "Webhook de pagamento", description = "Chamado pelo processador de pagamento")
@SecurityRequirement(name = OpenApiConfig.WEBHOOK_SECRET_SCHEME)
public class PaymentWebhookController {

    private final ProcessPaymentNotificationUseCase processPayment;

    public PaymentWebhookController(ProcessPaymentNotificationUseCase processPayment) {
        this.processPayment = processPayment;
    }

    @PostMapping
    @Operation(summary = "Informa se o pagamento foi efetuado (PAID) ou cancelado (CANCELED)")
    public SaleResponse notifyPayment(@Valid @RequestBody PaymentNotificationRequest request) {
        return SaleResponse.from(processPayment.execute(request.paymentCode(), request.status()));
    }
}
