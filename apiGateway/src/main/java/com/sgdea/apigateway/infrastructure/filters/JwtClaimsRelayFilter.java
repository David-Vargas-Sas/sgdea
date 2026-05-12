package com.sgdea.apigateway.infrastructure.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro global que extrae los claims del JWT ya validado por el Gateway
 * y los propaga como headers HTTP hacia los microservicios downstream.
 *
 * <p>Esto permite a los microservicios (multitenancy, administracion) conocer la
 * identidad del usuario autenticado sin necesidad de re-parsear el JWT.
 * Los headers eliminan los valores previos para evitar <em>header injection</em>.
 *
 * <p>Headers propagados:
 * <ul>
 *   <li>{@code X-User-Email}     — correo/sub del usuario</li>
 *   <li>{@code X-User-Id}        — ID numérico del usuario</li>
 *   <li>{@code X-User-Role}      — código del rol (ej. ADMIN)</li>
 *   <li>{@code X-Company-Id}     — UUID de la empresa</li>
 *   <li>{@code X-Company-Code}   — código corto de la empresa</li>
 *   <li>{@code X-Connection-Id}  — UUID de la conexión de BD</li>
 * </ul>
 *
 * <p>Para rutas públicas (sin JWT) el filtro no añade ningún header.
 */
@Component
public class JwtClaimsRelayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtClaimsRelayFilter.class);

    /**
     * Orden negativo para ejecutarse antes de los filtros de routing de Spring Cloud Gateway
     * pero después del filtro de seguridad de Spring Security (que autentica la solicitud).
     */
    private static final int ORDER = -50;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtAuth -> {
                    var jwt = jwtAuth.getToken();

                    log.debug("Propagando claims JWT al downstream: email={}, role={}",
                            jwt.getClaimAsString("email"),
                            jwt.getClaimAsString("roleCode"));

                    // Eliminar headers previos (evitar header injection desde clientes)
                    // y añadir los claims validados por el Gateway
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .headers(headers -> {
                                headers.remove("X-User-Email");
                                headers.remove("X-User-Id");
                                headers.remove("X-User-Role");
                                headers.remove("X-Company-Id");
                                headers.remove("X-Company-Code");
                                headers.remove("X-Connection-Id");
                            })
                            .header("X-User-Email",    getClaimAsString(jwt, "email"))
                            .header("X-User-Id",       getClaimAsString(jwt, "userId"))
                            .header("X-User-Role",     getClaimAsString(jwt, "roleCode"))
                            .header("X-Company-Id",    getClaimAsString(jwt, "companyId"))
                            .header("X-Company-Code",  getClaimAsString(jwt, "companyCode"))
                            .header("X-Connection-Id", getClaimAsString(jwt, "connectionId"))
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                // Rutas públicas (sin JWT): limpiar headers de identidad y continuar
                .switchIfEmpty(
                        chain.filter(
                                exchange.mutate()
                                        .request(exchange.getRequest().mutate()
                                                .headers(headers -> {
                                                    headers.remove("X-User-Email");
                                                    headers.remove("X-User-Id");
                                                    headers.remove("X-User-Role");
                                                    headers.remove("X-Company-Id");
                                                    headers.remove("X-Company-Code");
                                                    headers.remove("X-Connection-Id");
                                                })
                                                .build())
                                        .build()
                        )
                );
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    private String getClaimAsString(org.springframework.security.oauth2.jwt.Jwt jwt, String claimName) {
        Object claim = jwt.getClaim(claimName);
        return claim != null ? claim.toString() : "";
    }
}

