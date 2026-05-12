package com.sgdea.multitenancy.multitenancy.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginResponseDto {
    private String token;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
    private Long userId;
    private String email;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private UUID connectionId;
    private String connectionName;
    private String provider;
    private String databaseName;
}
