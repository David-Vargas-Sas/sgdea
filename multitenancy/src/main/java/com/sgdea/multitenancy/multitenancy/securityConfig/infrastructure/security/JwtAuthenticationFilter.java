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
import java.util.Map;
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
            Map<String, Object> claims = jwtTokenService.validateAndGetClaims(token);
            String email = getStringClaim(claims, "email");
            String roleCode = getStringClaim(claims, "roleCode");

            Optional<JwtSessionCacheDto> cached = jwtSessionCacheService.getSession(token);

            if (cached.isPresent()) {
                JwtSessionCacheDto dto = cached.get();
                if (isCachedSessionActive(dto)) {
                    setAuthentication(dto.getEmail(), dto.getRoleCode());
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                AuthSession session = authSessionRepository.findByToken(token)
                        .filter(this::isActive)
                        .orElseThrow(() -> new IllegalArgumentException("Sesion invalida"));

                jwtSessionCacheService.cacheSession(session, email, roleCode);
                setAuthentication(email, roleCode);
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

    private String getStringClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Token sin claim requerido: " + name);
        }
        return value.toString();
    }

    private boolean isActive(AuthSession session) {
        return Boolean.TRUE.equals(session.getActive())
                && session.getLoggedOutAt() == null
                && session.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
