package br.com.fiap.soat.vehiclesales.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String INTERNAL_TOKEN_SCHEME = "internalToken";
    public static final String WEBHOOK_SECRET_SCHEME = "webhookSecret";

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Sales Service")
                        .version("1.0.0")
                        .description("Serviço de venda de veículos: listagens, compra e webhook de pagamento."))
                .components(new Components()
                        .addSecuritySchemes(INTERNAL_TOKEN_SCHEME,
                                headerKey(SecurityConfig.INTERNAL_TOKEN_HEADER))
                        .addSecuritySchemes(WEBHOOK_SECRET_SCHEME,
                                headerKey(SecurityConfig.WEBHOOK_SECRET_HEADER)));
    }

    private static SecurityScheme headerKey(String headerName) {
        return new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name(headerName);
    }
}
