package com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security;

import com.sgdea.multitenancy.multitenancy.auth.domain.authSession.model.AuthSession;
import com.sgdea.multitenancy.multitenancy.auth.domain.authSession.repository.AuthSessionRepository;
import com.sgdea.multitenancy.multitenancy.securityConfig.application.dto.JwtSessionCacheDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AuthSessionRepository authSessionRepository;
    private final JwtSessionCacheService jwtSessionCacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. Validar firma y expiración del JWT (operación local, sin I/O)
            jwtTokenService.validateAndGetClaims(token);

            // 2. Intentar obtener la sesión desde Redis (cache-aside)
            Optional<JwtSessionCacheDto> cached = jwtSessionCacheService.getSession(token);

            if (cached.isPresent()) {
                // Cache HIT: validar que la sesión aún está activa
                JwtSessionCacheDto dto = cached.get();
                if (isCachedSessionActive(dto)) {
                    setAuthentication(dto.getEmail(), dto.getRoleCode());
                } else {
                    // Sesión invalidada (logout) pero aún en caché como inactiva
                    SecurityContextHolder.clearContext();
                }
            } else {
                // Cache MISS: consultar BD y poblar el caché
                AuthSession session = authSessionRepository.findByToken(token)
                        .filter(this::isActive)
                        .orElseThrow(() -> new IllegalArgumentException("Sesion invalida"));

                // Guardar en Redis para futuros requests
                jwtSessionCacheService.cacheSession(session);

                setAuthentication(session.getUser().getEmail(), session.getUser().getRole().getCode());
            }
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String email, String roleCode) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleCode))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isCachedSessionActive(JwtSessionCacheDto dto) {
        return Boolean.TRUE.equals(dto.getActive())
                && dto.getLoggedOutAt() == null
                && dto.getExpiresAt() != null
                && dto.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private String getBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }

    private boolean isActive(AuthSession session) {
        return Boolean.TRUE.equals(session.getActive())
                && session.getLoggedOutAt() == null
                && session.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
