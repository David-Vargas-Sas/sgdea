package com.sgdea.multitenancy.multitenancy.securityConfig.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO serializable que representa los datos de sesión JWT almacenados en Redis.
 * Evita la dependencia de entidades JPA con relaciones LAZY al momento de serializar.
 *
 * <p>Incluye los claims de tenant ({@code companyId}, {@code companyCode},
 * {@code connectionId}, {@code userId}) que el JWT contiene y que son propagados
 * por el API Gateway como headers {@code X-Company-Id}, {@code X-Connection-Id}, etc.
 * Al cachearlos aquí se evita re-parsear el token o volver a la BD en cada request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtSessionCacheDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private String token;
    private String email;
    private String userId;
    private String roleCode;
    private String companyId;
    private String companyCode;
    private String connectionId;
    private Boolean active;
    private LocalDateTime expiresAt;
    private LocalDateTime loggedOutAt;
}
