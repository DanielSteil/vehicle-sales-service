package br.com.fiap.soat.vehiclesales.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Segredos obrigatórios: a aplicação não sobe sem eles. */
@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(@NotBlank String internalToken, @NotBlank String webhookSecret) {
}
