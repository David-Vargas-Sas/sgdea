package com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security;

import com.sgdea.multitenancy.multitenancy.auth.domain.authSession.model.AuthSession;
import com.sgdea.multitenancy.multitenancy.securityConfig.application.dto.JwtSessionCacheDto;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class JwtSessionCacheService {

    private static final Logger log = LoggerFactory.getLogger(JwtSessionCacheService.class);
    private static final String KEY_PREFIX = "jwt:session:";

    private final RedisTemplate<String, JwtSessionCacheDto> redisTemplate;

    public void cacheSession(AuthSession session) {
        cacheSession(
                session.getToken(),
                session.getUser().getEmail(),
                session.getUser().getRole().getCode(),
                session.getActive(),
                session.getExpiresAt(),
                session.getLoggedOutAt()
        );
    }

    public void cacheSession(AuthSession session, String email, String roleCode) {
        cacheSession(
                session.getToken(),
                email,
                roleCode,
                session.getActive(),
                session.getExpiresAt(),
                session.getLoggedOutAt()
        );
    }

    public void cacheSession(
            String token,
            String email,
            String roleCode,
            Boolean active,
            LocalDateTime expiresAt,
            LocalDateTime loggedOutAt) {
        try {
            Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);
            if (ttl.isNegative() || ttl.isZero()) {
                log.debug("Sesion ya expirada, no se almacena en Redis: token={}...", abbreviate(token));
                return;
            }

            JwtSessionCacheDto dto = new JwtSessionCacheDto(
                    token,
                    email,
                    roleCode,
                    active,
                    expiresAt,
                    loggedOutAt
            );

            redisTemplate.opsForValue().set(buildKey(token), dto, ttl);
            log.debug("Sesion cacheada en Redis con TTL={}s: token={}...", ttl.getSeconds(), abbreviate(token));
        } catch (Exception ex) {
            log.warn("No se pudo cachear la sesion en Redis: {}", ex.getMessage());
        }
    }

    public Optional<JwtSessionCacheDto> getSession(String token) {
        try {
            JwtSessionCacheDto cached = redisTemplate.opsForValue().get(buildKey(token));
            if (cached != null) {
                log.debug("Cache HIT en Redis: token={}...", abbreviate(token));
                return Optional.of(cached);
            }
            log.debug("Cache MISS en Redis: token={}...", abbreviate(token));
        } catch (Exception ex) {
            log.warn("No se pudo consultar Redis, se usara BD: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    public void evictSession(String token) {
        try {
            Boolean deleted = redisTemplate.delete(buildKey(token));
            log.debug("Sesion eliminada de Redis (deleted={}): token={}...", deleted, abbreviate(token));
        } catch (Exception ex) {
            log.warn("No se pudo eliminar la sesion de Redis: {}", ex.getMessage());
        }
    }

    public void markSessionInactive(String token) {
        try {
            String key = buildKey(token);
            JwtSessionCacheDto cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                cached.setActive(false);
                cached.setLoggedOutAt(LocalDateTime.now());
                redisTemplate.opsForValue().set(key, cached, Duration.ofSeconds(60));
                log.debug("Sesion marcada como inactiva en Redis: token={}...", abbreviate(token));
            } else {
                evictSession(token);
            }
        } catch (Exception ex) {
            log.warn("No se pudo actualizar el estado de la sesion en Redis: {}", ex.getMessage());
        }
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }

    private String abbreviate(String token) {
        return token != null && token.length() > 20 ? token.substring(0, 20) : token;
    }
}
