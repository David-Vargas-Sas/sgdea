package com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.usecase;

import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionCreateDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CompanyDatabaseConnectionUseCase {
    List<CompanyDatabaseConnectionResponseDto> findAll();
    CompanyDatabaseConnectionResponseDto findById(UUID id);
    List<CompanyDatabaseConnectionResponseDto> findByCompanyId(UUID companyId);
    Page<CompanyDatabaseConnectionResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection);
    CompanyDatabaseConnectionResponseDto create(CompanyDatabaseConnectionCreateDto dto);
    CompanyDatabaseConnectionResponseDto update(UUID id, CompanyDatabaseConnectionUpdateDto dto);
    Boolean delete(UUID id);
    String toggleActive(UUID id);
}
