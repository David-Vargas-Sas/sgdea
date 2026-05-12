package com.sgdea.apigateway.infrastructure.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Configuración de seguridad del API Gateway.
 *
 * <p>El Gateway actúa como <strong>único punto de validación JWT</strong> para todas
 * las peticiones entrantes. Usa el secreto HMAC-SHA256 compartido con multitenancy
 * para verificar la firma de los tokens sin necesidad de un servidor OIDC externo.
 *
 * <p>Rutas públicas (sin JWT requerido):
 * <ul>
 *   <li>{@code POST /multitenancy/auth/login} — inicio de sesión</li>
 *   <li>{@code POST /multitenancy/auth/refresh} — renovación de token</li>
 *   <li>{@code POST /multitenancy/auth/logout} — cierre de sesión</li>
 *   <li>{@code /actuator/health}, {@code /actuator/info} — health checks</li>
 * </ul>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        // ── Rutas públicas de autenticación ──────────────────────────────────
                        // Login y refresh no llevan JWT (el usuario aún no lo tiene)
                        .pathMatchers(
                                "/multitenancy/auth/login",
                                "/multitenancy/auth/refresh"
                        ).permitAll()

                        // Logout: se permite sin JWT para cubrir tokens ya expirados;
                        // el microservicio valida el token en el cuerpo/header.
                        .pathMatchers("/multitenancy/auth/logout").permitAll()

                        // Health checks para orquestadores (K8s liveness/readiness)
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()

                        // ── Todo lo demás requiere JWT válido ─────────────────────────────────
                        .anyExchange().authenticated()
                )

                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                // Usar el decoder con secreto HMAC-SHA256 compartido
                                .jwtDecoder(jwtDecoder)
                                // Converter que extrae roleCode → ROLE_xxx y usa email como principal
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )

                .build();
    }
}
