package com.sgdea.multitenancy.multitenancy.infraestructure.security;

import com.sgdea.multitenancy.multitenancy.domain.authSession.model.AuthSession;
import com.sgdea.multitenancy.multitenancy.domain.authSession.repository.AuthSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    private final AuthSessionRepository authSessionRepository;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, AuthSessionRepository authSessionRepository) {
        this.jwtTokenService = jwtTokenService;
        this.authSessionRepository = authSessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwtTokenService.validateAndGetClaims(token);
            AuthSession session = authSessionRepository.findByToken(token)
                    .filter(this::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("Sesion invalida"));

            String roleCode = session.getUser().getRole().getCode();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    session.getUser().getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + roleCode))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
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
