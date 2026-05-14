package com.sgdea.administracion.infrastructure.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
/**
 * Filtro de autenticacion basado en los headers propagados por el API Gateway.
 *
 * El Gateway valida la firma JWT; una vez que la peticion llega aqui, los headers
 * X-User-* son headers de confianza internos. Este microservicio NO re-valida la
 * firma JWT, delegando esa responsabilidad al Gateway.
 *
 * Headers esperados (propagados por JwtClaimsRelayFilter del Gateway):
 *   X-User-Email     correo del usuario autenticado
 *   X-User-Id        ID numerico del usuario
 *   X-User-Role      codigo del rol (ej. ADMIN)
 *   X-Company-Id     UUID de la empresa (tenant)
 *   X-Company-Code   codigo corto de la empresa
 *   X-Connection-Id  UUID de la conexion de BD del tenant
 *
 * Seguridad: la red interna debe garantizar que solo el Gateway puede llegar a este
 * microservicio (VPC, NetworkPolicy de K8s, firewall, etc.).
 */
@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(GatewayAuthenticationFilter.class);
    private static final String HEADER_EMAIL         = "X-User-Email";
    private static final String HEADER_USER_ID       = "X-User-Id";
    private static final String HEADER_ROLE          = "X-User-Role";
    private static final String HEADER_COMPANY_ID    = "X-Company-Id";
    private static final String HEADER_COMPANY_CODE  = "X-Company-Code";
    private static final String HEADER_CONNECTION_ID = "X-Connection-Id";
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String email = request.getHeader(HEADER_EMAIL);
            if (email != null && !email.isBlank()) {
                String userId       = nullSafe(request.getHeader(HEADER_USER_ID));
                String roleCode     = nullSafe(request.getHeader(HEADER_ROLE));
                String companyId    = nullSafe(request.getHeader(HEADER_COMPANY_ID));
                String companyCode  = nullSafe(request.getHeader(HEADER_COMPANY_CODE));
                String connectionId = nullSafe(request.getHeader(HEADER_CONNECTION_ID));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                roleCode.isBlank()
                                        ? List.of()
                                        : List.of(new SimpleGrantedAuthority("ROLE_" + roleCode))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                GatewayClaimsHolder.set(new GatewayClaimsHolder.GatewayClaims(
                        email, userId, roleCode, companyId, companyCode, connectionId
                ));
                log.debug("Auth desde Gateway headers: email={}, role={}, company={}, connection={}",
                        email, roleCode, companyId, connectionId);
            } else {
                SecurityContextHolder.clearContext();
                log.debug("Request sin headers del Gateway: uri={}", request.getRequestURI());
            }
            filterChain.doFilter(request, response);
        } finally {
            // Siempre limpiar el ThreadLocal para evitar fugas en el pool de hilos
            GatewayClaimsHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }
    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
