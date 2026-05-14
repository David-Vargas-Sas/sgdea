package com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security;

import com.sgdea.multitenancy.multitenancy.auth.domain.authSession.model.AuthSession;
import com.sgdea.multitenancy.multitenancy.auth.domain.authSession.repository.AuthSessionRepository;
import com.sgdea.multitenancy.multitenancy.securityConfig.application.dto.JwtSessionCacheDto;
import com.sgdea.multitenancy.multitenancy.securityConfig.domain.model.GatewayClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final AuthSessionRepository authSessionRepository;
    private final JwtSessionCacheService jwtSessionCacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);
        if (token != null) {
            try {
                processAuthentication(token);
            } catch (Exception ex) {
                log.debug("Autenticación JWT fallida [{}]: {}", request.getRequestURI(), ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Limpiar siempre el ThreadLocal para evitar fugas en el pool de hilos
            GatewayClaimsHolder.clear();
        }
    }

    /**
     * Núcleo del flujo: valida el JWT, consulta caché/BD y construye el contexto de seguridad.
     * Lanza excepción si la sesión es inválida o inactiva.
     */
    private void processAuthentication(String token) {
        // 1. Validar firma, expiración e issuer del JWT (siempre obligatorio)
        Map<String, Object> rawClaims = jwtTokenService.validateAndGetClaims(token);

        // 2. Cache HIT: evitar round-trip a BD
        Optional<JwtSessionCacheDto> cached = jwtSessionCacheService.getSession(token);
        if (cached.isPresent()) {
            JwtSessionCacheDto dto = cached.get();
            if (!isCachedSessionActive(dto)) {
                throw new IllegalStateException("Sesión inactiva (caché)");
            }
            applyAuthentication(fromDto(dto));
            return;
        }

        // 3. Cache MISS: consultar BD, cachear y autenticar
        AuthSession session = authSessionRepository.findByToken(token)
                .filter(this::isSessionActive)
                .orElseThrow(() -> new IllegalStateException("Sesión inválida o inactiva (BD)"));

        GatewayClaims claims = toClaims(rawClaims);
        jwtSessionCacheService.cacheSession(session,
                claims.email(), claims.userId(), claims.roleCode(),
                claims.companyId(), claims.companyCode(), claims.connectionId());
        applyAuthentication(claims);
    }

    /**
     * Establece el SecurityContext y el GatewayClaimsHolder en un único punto.
     * Recibe {@link GatewayClaims} directamente para evitar duplicar email y roleCode
     * como parámetros separados cuando ya están disponibles en el record.
     */
    private void applyAuthentication(GatewayClaims claims) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                claims.email(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + claims.roleCode()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        GatewayClaimsHolder.set(claims);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    /** Construye GatewayClaims desde los claims crudos del JWT (path sin caché). */
    private GatewayClaims toClaims(Map<String, Object> raw) {
        return new GatewayClaims(
                requireClaim(raw, "email"),
                optionalClaim(raw, "userId"),
                requireClaim(raw, "roleCode"),
                optionalClaim(raw, "companyId"),
                optionalClaim(raw, "companyCode"),
                optionalClaim(raw, "connectionId")
        );
    }

    /** Construye GatewayClaims desde el DTO en caché (path con Redis HIT). */
    private GatewayClaims fromDto(JwtSessionCacheDto dto) {
        return new GatewayClaims(
                safe(dto.getEmail()),
                safe(dto.getUserId()),
                safe(dto.getRoleCode()),
                safe(dto.getCompanyId()),
                safe(dto.getCompanyCode()),
                safe(dto.getConnectionId())
        );
    }

    // ── Validación de sesión ─────────────────────────────────────────────────

    private boolean isCachedSessionActive(JwtSessionCacheDto dto) {
        return Boolean.TRUE.equals(dto.getActive())
                && dto.getLoggedOutAt() == null
                && dto.getExpiresAt() != null
                && dto.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private boolean isSessionActive(AuthSession session) {
        return Boolean.TRUE.equals(session.getActive())
                && session.getLoggedOutAt() == null
                && session.getExpiresAt() != null                 // guarda NPE
                && session.getExpiresAt().isAfter(LocalDateTime.now());
    }

    // ── Helpers de extracción ────────────────────────────────────────────────

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private String requireClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Claim requerido ausente: " + name);
        }
        return value.toString();
    }

    private String optionalClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        return value != null ? value.toString() : "";
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
