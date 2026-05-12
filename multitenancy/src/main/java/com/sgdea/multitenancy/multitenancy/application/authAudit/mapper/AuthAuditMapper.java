package com.sgdea.multitenancy.multitenancy.application.authAudit.mapper;

import com.sgdea.multitenancy.multitenancy.application.authAudit.dto.AuthAuditResponseDto;
import com.sgdea.multitenancy.multitenancy.domain.authAudit.model.AuthAudit;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditMapper {
    public AuthAuditResponseDto toResponse(AuthAudit audit) {
        return AuthAuditResponseDto.builder()
                .id(audit.getId())
                .eventType(audit.getEventType())
                .success(audit.getSuccess())
                .message(audit.getMessage())
                .email(audit.getEmail())
                .userId(audit.getUser() != null ? audit.getUser().getId() : null)
                .companyId(audit.getCompany() != null ? audit.getCompany().getId() : null)
                .companyName(audit.getCompany() != null ? audit.getCompany().getName() : null)
                .ipAddress(audit.getIpAddress())
                .userAgent(audit.getUserAgent())
                .createdAt(audit.getCreatedAt())
                .build();
    }
}
