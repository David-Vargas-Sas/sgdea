package com.sgdea.multitenancy.multitenancy.auth.application.service;

import com.sgdea.multitenancy.multitenancy.auth.application.dto.AuthLoginRequestDto;
import com.sgdea.multitenancy.multitenancy.auth.application.dto.AuthLoginResponseDto;
import com.sgdea.multitenancy.multitenancy.auth.application.dto.AuthLogoutRequestDto;
import com.sgdea.multitenancy.multitenancy.auth.application.dto.AuthRefreshRequestDto;
import com.sgdea.multitenancy.multitenancy.auth.application.usecase.AuthUseCase;
import com.sgdea.multitenancy.multitenancy.auth.domain.authSession.model.AuthSession;
import com.sgdea.multitenancy.multitenancy.auth.domain.authSession.repository.AuthSessionRepository;
import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.model.CompanyDatabaseConnection;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.repository.CompanyDatabaseConnectionRepository;
import com.sgdea.multitenancy.multitenancy.companyUser.domain.model.CompanyUser;
import com.sgdea.multitenancy.multitenancy.companyUser.domain.repository.CompanyUserRepository;
import com.sgdea.multitenancy.multitenancy.user.domain.model.User;
import com.sgdea.multitenancy.multitenancy.user.domain.repository.UserRepository;
import com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security.JwtTokenService;
import com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security.JwtSessionCacheService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService implements AuthUseCase {
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final CompanyDatabaseConnectionRepository connectionRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtSessionCacheService jwtSessionCacheService;
    private final int accessTokenMinutes;
    private final int refreshTokenHours;

    public AuthService(
            UserRepository userRepository,
            CompanyUserRepository companyUserRepository,
            CompanyDatabaseConnectionRepository connectionRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            JwtSessionCacheService jwtSessionCacheService,
            @Value("${security.jwt.access-token-minutes:30}") int accessTokenMinutes,
            @Value("${security.jwt.refresh-token-hours:8}") int refreshTokenHours) {
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.connectionRepository = connectionRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtSessionCacheService = jwtSessionCacheService;
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenHours = refreshTokenHours;
    }

    @Override
    @Transactional
    public AuthLoginResponseDto login(AuthLoginRequestDto dto) {
        User user = userRepository.findByEmailIgnoreCase(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales invalidas");
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalArgumentException("El usuario esta inactivo");
        }

        CompanyUser companyUser = getActiveCompanyUser(user.getId());
        Company company = companyUser.getCompany();
        if (!Boolean.TRUE.equals(company.getActive())) {
            throw new IllegalArgumentException("La empresa asociada esta inactiva");
        }

        CompanyDatabaseConnection connection = getDefaultActiveConnection(company.getId());
        closeActiveSessions(user.getId());
        AuthSession session = createSession(user, company, connection);
        // Cachear la sesión en Redis con todos los claims de tenant para evitar consultas a BD
        jwtSessionCacheService.cacheSession(session,
                user.getEmail(),
                user.getId() != null ? user.getId().toString() : "",
                user.getRole().getCode(),
                company.getId() != null ? company.getId().toString() : "",
                company.getCode() != null ? company.getCode() : "",
                connection.getId() != null ? connection.getId().toString() : "");
        return buildLoginResponse(session);
    }

    @Override
    @Transactional
    public AuthLoginResponseDto refresh(AuthRefreshRequestDto dto) {
        AuthSession session = authSessionRepository.findByRefreshToken(dto.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token invalido"));

        if (!isRefreshSessionValid(session)) {
            throw new IllegalArgumentException("Refresh token invalido o expirado");
        }

        LocalDateTime accessExpiresAt = LocalDateTime.now().plusMinutes(accessTokenMinutes);
        session.setToken(jwtTokenService.generateToken(session.getUser(), session.getCompany(), session.getConnection(), accessExpiresAt));
        session.setExpiresAt(accessExpiresAt);
        AuthSession saved = authSessionRepository.save(session);
        // Actualizar el caché con el nuevo token y todos los claims de tenant
        User refreshUser = saved.getUser();
        Company refreshCompany = saved.getCompany();
        CompanyDatabaseConnection refreshConnection = saved.getConnection();
        jwtSessionCacheService.cacheSession(saved,
                refreshUser.getEmail(),
                refreshUser.getId() != null ? refreshUser.getId().toString() : "",
                refreshUser.getRole().getCode(),
                refreshCompany.getId() != null ? refreshCompany.getId().toString() : "",
                refreshCompany.getCode() != null ? refreshCompany.getCode() : "",
                refreshConnection.getId() != null ? refreshConnection.getId().toString() : "");
        return buildLoginResponse(saved);
    }

    @Override
    @Transactional
    public Boolean logout(AuthLogoutRequestDto dto) {
        if (dto == null || dto.getToken() == null || dto.getToken().isBlank()) {
            throw new IllegalArgumentException("El token es obligatorio");
        }
        return logout(dto.getToken());
    }

    @Override
    @Transactional
    public Boolean logout(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token es obligatorio");
        }

        AuthSession session = authSessionRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("No existe una sesion activa con el token enviado"));

        if (!Boolean.TRUE.equals(session.getActive())) {
            return true;
        }

        session.setActive(false);
        session.setLoggedOutAt(LocalDateTime.now());
        authSessionRepository.save(session);
        // Invalidar la sesión en Redis (marcar como inactiva para absorber requests en vuelo)
        jwtSessionCacheService.markSessionInactive(token);
        return true;
    }

    /**
     * Cierra todas las sesiones activas de un usuario.
     *
     * <p><b>Corrección N+1</b>: el patrón anterior hacía 1 SELECT de entidades completas
     * más N UPDATEs individuales (uno por sesión). Ahora se ejecutan exactamente
     * <strong>2 queries</strong> independientemente de cuántas sesiones tenga el usuario:
     * <ol>
     *   <li>SELECT de tokens (proyección escalar) → invalidar caché Redis.</li>
     *   <li>UPDATE masivo con {@code @Modifying @Query} → una sola sentencia SQL.</li>
     * </ol>
     */
    private void closeActiveSessions(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Obtener solo los tokens (proyección escalar) para evicción Redis.
        //    No carga entidades completas ni relaciones LAZY.
        List<String> activeTokens = authSessionRepository.findActiveTokensByUserId(userId);

        // 2. UPDATE masivo: una sola sentencia SQL sin importar cuántas sesiones haya.
        authSessionRepository.deactivateSessionsByUserId(userId, now);

        // 3. Evictar del caché Redis los tokens invalidados.
        //    (operación local — no genera queries adicionales a BD)
        activeTokens.forEach(jwtSessionCacheService::evictSession);
    }

    private CompanyUser getActiveCompanyUser(Long userId) {
        return companyUserRepository.findByUserId(userId)
                .stream()
                .filter(companyUser -> Boolean.TRUE.equals(companyUser.getActive()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El usuario no tiene una empresa activa asociada"));
    }

    private CompanyDatabaseConnection getDefaultActiveConnection(UUID companyId) {
        return connectionRepository.findByCompanyIdAndDefaultConnectionTrue(companyId)
                .stream()
                .filter(connection -> Boolean.TRUE.equals(connection.getActive()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La empresa no tiene una conexion de base de datos activa por defecto"));
    }

    private AuthSession createSession(User user, Company company, CompanyDatabaseConnection connection) {
        AuthSession session = new AuthSession();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(accessTokenMinutes);
        LocalDateTime refreshExpiresAt = LocalDateTime.now().plusHours(refreshTokenHours);
        session.setToken(jwtTokenService.generateToken(user, company, connection, expiresAt));
        session.setRefreshToken(UUID.randomUUID().toString());
        session.setUser(user);
        session.setCompany(company);
        session.setConnection(connection);
        session.setExpiresAt(expiresAt);
        session.setRefreshExpiresAt(refreshExpiresAt);
        return authSessionRepository.save(session);
    }

    private boolean isRefreshSessionValid(AuthSession session) {
        return Boolean.TRUE.equals(session.getActive())
                && session.getLoggedOutAt() == null
                && session.getRefreshExpiresAt().isAfter(LocalDateTime.now())
                && Boolean.TRUE.equals(session.getUser().getActive())
                && Boolean.TRUE.equals(session.getCompany().getActive())
                && Boolean.TRUE.equals(session.getConnection().getActive());
    }

    private AuthLoginResponseDto buildLoginResponse(AuthSession session) {
        User user = session.getUser();
        Company company = session.getCompany();
        return AuthLoginResponseDto.builder()
                .token(session.getToken())
                .refreshToken(session.getRefreshToken())
                .expiresAt(session.getExpiresAt())
                .refreshExpiresAt(session.getRefreshExpiresAt())
                .userId(user.getId())
                .email(user.getEmail())
                .documentNumber(user.getDocumentNumber())
                .firstName(user.getFirstName())
                .secondName(user.getSecondName())
                .firstLastName(user.getFirstLastName())
                .secondLastName(user.getSecondLastName())
                .roleId(user.getRole().getId())
                .roleCode(user.getRole().getCode())
                .roleName(user.getRole().getName())
                .companyId(company.getId())
                .companyCode(company.getCode())
                .companyName(company.getName())
                .build();
    }
}
