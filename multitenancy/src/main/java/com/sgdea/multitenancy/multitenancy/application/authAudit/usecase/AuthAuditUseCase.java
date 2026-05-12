package com.sgdea.multitenancy.multitenancy.application.authAudit.usecase;

import com.sgdea.multitenancy.multitenancy.application.authAudit.dto.AuthAuditResponseDto;
import org.springframework.data.domain.Page;

public interface AuthAuditUseCase {
    Page<AuthAuditResponseDto> findAllPaginated(int page, int size);
}
