package com.sgdea.multitenancy.multitenancy.application.authAudit.dto;

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
public class AuthAuditResponseDto {
    private UUID id;
    private String eventType;
    private Boolean success;
    private String message;
    private String email;
    private Long userId;
    private UUID companyId;
    private String companyName;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
