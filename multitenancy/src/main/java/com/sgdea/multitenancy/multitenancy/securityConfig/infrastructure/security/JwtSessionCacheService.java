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

/**
 * Servicio que gestiona el caché de sesiones JWT en Redis.
 *
 * <p>Estrategia Cache-Aside:
 * <ul>
 *   <li>Login / Refresh → guarda la sesión en Redis con TTL igual a expiresAt.</li>
 *   <li>Logout → elimina la entrada de Redis.</li>
 *   <li>Validación por request → consulta Redis primero; si no está, va a BD y cachea.</li>
 * </ul>
 */
@Service
@AllArgsConstructor
public class JwtSessionCacheService {

    private static final Logger log = LoggerFactory.getLogger(JwtSessionCacheService.class);
    private static final String KEY_PREFIX = "jwt:session:";

    private final RedisTemplate<String, JwtSessionCacheDto> redisTemplate;

    /**
     * Almacena una sesión JWT en Redis con TTL calculado a partir de {@code expiresAt}.
     * Si {@code expiresAt} ya pasó, no se almacena.
     */
    public void cacheSession(AuthSession session) {
        try {
            Duration ttl = Duration.between(LocalDateTime.now(), session.getExpiresAt());
            if (ttl.isNegative() || ttl.isZero()) {
                log.debug("Sesión ya expirada, no se almacena en Redis: token={}...", abbreviate(session.getToken()));
                return;
            }

            JwtSessionCacheDto dto = new JwtSessionCacheDto(
                    session.getToken(),
                    session.getUser().getEmail(),
                    session.getUser().getRole().getCode(),
                    session.getActive(),
                    session.getExpiresAt(),
                    session.getLoggedOutAt()
            );

            redisTemplate.opsForValue().set(buildKey(session.getToken()), dto, ttl);
            log.debug("Sesión cacheada en Redis con TTL={}s: token={}...", ttl.getSeconds(), abbreviate(session.getToken()));
        } catch (Exception ex) {
            // No fallar el flujo principal si Redis no está disponible
            log.warn("No se pudo cachear la sesión en Redis: {}", ex.getMessage());
        }
    }

    /**
     * Recupera una sesión cacheada desde Redis a partir del token.
     */
    public Optional<JwtSessionCacheDto> getSession(String token) {
        try {
            JwtSessionCacheDto cached = redisTemplate.opsForValue().get(buildKey(token));
            if (cached != null) {
                log.debug("Cache HIT en Redis: token={}...", abbreviate(token));
                return Optional.of(cached);
            }
            log.debug("Cache MISS en Redis: token={}...", abbreviate(token));
        } catch (Exception ex) {
            log.warn("No se pudo consultar Redis, se usará BD: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Elimina la sesión del caché (usado en logout o invalidación).
     */
    public void evictSession(String token) {
        try {
            Boolean deleted = redisTemplate.delete(buildKey(token));
            log.debug("Sesión eliminada de Redis (deleted={}): token={}...", deleted, abbreviate(token));
        } catch (Exception ex) {
            log.warn("No se pudo eliminar la sesión de Redis: {}", ex.getMessage());
        }
    }

    /**
     * Actualiza el caché marcando la sesión como inactiva (logout suave).
     * Mantiene la entrada con un TTL mínimo para evitar que el token quede "sin registro".
     */
    public void markSessionInactive(String token) {
        try {
            String key = buildKey(token);
            JwtSessionCacheDto cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                cached.setActive(false);
                cached.setLoggedOutAt(LocalDateTime.now());
                // Mantener en Redis 60 segundos adicionales para absorber requests en vuelo
                redisTemplate.opsForValue().set(key, cached, Duration.ofSeconds(60));
                log.debug("Sesión marcada como inactiva en Redis: token={}...", abbreviate(token));
            } else {
                evictSession(token);
            }
        } catch (Exception ex) {
            log.warn("No se pudo actualizar el estado de la sesión en Redis: {}", ex.getMessage());
        }
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }

    private String abbreviate(String token) {
        return token != null && token.length() > 20 ? token.substring(0, 20) : token;
    }
}

