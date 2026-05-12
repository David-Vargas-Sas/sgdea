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
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtSessionCacheDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String token;
    private String email;
    private String roleCode;
    private Boolean active;
    private LocalDateTime expiresAt;
    private LocalDateTime loggedOutAt;
}
