package com.sgdea.multitenancy.multitenancy.authAudit.application.usecase;

import com.sgdea.multitenancy.multitenancy.authAudit.application.dto.AuthAuditResponseDto;
import org.springframework.data.domain.Page;

public interface AuthAuditUseCase {
    Page<AuthAuditResponseDto> findAllPaginated(int page, int size);
}
