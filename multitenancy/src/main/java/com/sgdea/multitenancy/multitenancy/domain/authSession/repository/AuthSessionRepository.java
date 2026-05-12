package com.sgdea.multitenancy.multitenancy.domain.authSession.repository;

import com.sgdea.multitenancy.multitenancy.domain.authSession.model.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByToken(String token);

    Optional<AuthSession> findByRefreshToken(String refreshToken);

    List<AuthSession> findByUserIdAndActiveTrue(Long userId);
}
