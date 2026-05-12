package com.sgdea.multitenancy.multitenancy.auth.domain.authSession.repository;

import com.sgdea.multitenancy.multitenancy.auth.domain.authSession.model.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByToken(String token);

    Optional<AuthSession> findByRefreshToken(String refreshToken);

    /**
     * Devuelve únicamente los tokens de acceso de las sesiones activas del usuario.
     * Se usa para invalidar el caché Redis antes del UPDATE masivo.
     */
    @Query("SELECT s.token FROM AuthSession s WHERE s.user.id = :userId AND s.active = true")
    List<String> findActiveTokensByUserId(@Param("userId") Long userId);

    /**
     * Desactiva en masa todas las sesiones activas de un usuario con una sola
     * sentencia UPDATE. Evita el problema N+1 del patrón anterior
     * (load-all → for-each save).
     *
     * @param userId      ID del usuario cuyas sesiones se desactivarán.
     * @param loggedOutAt Timestamp para el campo {@code logged_out_at}.
     * @return Número de filas afectadas.
     */
    @Modifying
    @Query("UPDATE AuthSession s SET s.active = false, s.loggedOutAt = :loggedOutAt " +
           "WHERE s.user.id = :userId AND s.active = true")
    int deactivateSessionsByUserId(@Param("userId") Long userId,
                                   @Param("loggedOutAt") LocalDateTime loggedOutAt);
}
