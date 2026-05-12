package com.sgdea.apigateway.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Configuración del KeyResolver para el rate limiter de Spring Cloud Gateway.
 *
 * <p>Estrategia de clave:
 * <ol>
 *   <li>Si el header {@code X-User-Email} está presente (rutas autenticadas,
 *       inyectado por {@code JwtClaimsRelayFilter}) → rate limit <strong>por usuario</strong>.
 *       Esto evita que un usuario penalice a otros que comparten la misma IP (proxies, NATs).</li>
 *   <li>Si no hay email (rutas públicas: login, refresh) → rate limit <strong>por IP</strong>
 *       usando {@code X-Forwarded-For} o la dirección remota. Protege contra fuerza bruta.</li>
 * </ol>
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userOrIpKeyResolver() {
        return this::resolveKey;
    }

    private Mono<String> resolveKey(ServerWebExchange exchange) {
        // 1. Rutas autenticadas → rate limit por email (inyectado por JwtClaimsRelayFilter)
        String userEmail = exchange.getRequest().getHeaders().getFirst("X-User-Email");
        if (userEmail != null && !userEmail.isBlank()) {
            return Mono.just("user:" + userEmail);
        }

        // 2. Rutas públicas (login/refresh) → rate limit por IP del cliente
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // Tomar la primera IP (IP real del cliente detrás de proxies/load balancers)
            return Mono.just("ip:" + xff.split(",")[0].trim());
        }

        if (exchange.getRequest().getRemoteAddress() != null) {
            return Mono.just("ip:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        }

        return Mono.just("ip:unknown");
    }
}
