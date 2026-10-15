package br.com.fiap.soat.vehiclesales.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Exige um segredo compartilhado num header. Usado no endpoint interno (chamado pelo
 * serviço principal) e no webhook (chamado pelo processador de pagamento).
 */
public class ApiKeyFilter extends OncePerRequestFilter {

    private final String headerName;
    private final byte[] expectedKey;

    public ApiKeyFilter(String headerName, String expectedKey) {
        this.headerName = headerName;
        this.expectedKey = expectedKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(headerName);
        // comparação em tempo constante, para não vazar o segredo por diferença de tempo de resposta
        if (provided != null && MessageDigest.isEqual(expectedKey, provided.getBytes(StandardCharsets.UTF_8))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"type":"about:blank","title":"Não autorizado","status":401,"detail":"Header %s ausente ou inválido"}"""
                .formatted(headerName));
    }
}
