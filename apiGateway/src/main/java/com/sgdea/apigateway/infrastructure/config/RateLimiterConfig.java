package com.sgdea.apigateway.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ServerWebExchange;

@Configuration
public class RateLimiterConfig {

    /**
     * KeyResolver que usa el encabezado X-Forwarded-For o la dirección remota como clave
     * para el rate limiter. Esto permite limitar por IP del cliente.
     */
    @Bean
    public KeyResolver remoteAddressKeyResolver() {
        return exchange -> resolveKey(exchange);
    }

    private Mono<String> resolveKey(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // toma la primera IP listada
            String first = xff.split(",")[0].trim();
            return Mono.just(first);
        }

        if (exchange.getRequest().getRemoteAddress() != null) {
            return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        }

        return Mono.just("unknown");
    }
}

