package br.com.fiap.soat.vehiclesales.infrastructure.config;

import br.com.fiap.soat.vehiclesales.infrastructure.security.ApiKeyFilter;
import br.com.fiap.soat.vehiclesales.infrastructure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    public static final String WEBHOOK_SECRET_HEADER = "X-Webhook-Secret";

    @Bean
    FilterRegistrationBean<ApiKeyFilter> internalApiKeyFilter(SecurityProperties properties) {
        return filterFor("/internal/*", new ApiKeyFilter(INTERNAL_TOKEN_HEADER, properties.internalToken()));
    }

    @Bean
    FilterRegistrationBean<ApiKeyFilter> webhookSecretFilter(SecurityProperties properties) {
        return filterFor("/webhooks/*", new ApiKeyFilter(WEBHOOK_SECRET_HEADER, properties.webhookSecret()));
    }

    private static FilterRegistrationBean<ApiKeyFilter> filterFor(String urlPattern, ApiKeyFilter filter) {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns(urlPattern);
        return registration;
    }
}
