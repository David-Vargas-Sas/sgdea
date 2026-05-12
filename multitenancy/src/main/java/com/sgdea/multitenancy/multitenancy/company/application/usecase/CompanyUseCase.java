package com.sgdea.multitenancy.multitenancy.company.application.usecase;

import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyCreateDto;
import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CompanyUseCase {
    List<CompanyResponseDto> findAll();
    CompanyResponseDto findById(UUID id);
    CompanyResponseDto findByCode(String code);
    Page<CompanyResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection);
    CompanyResponseDto create(CompanyCreateDto dto);
    CompanyResponseDto update(UUID id, CompanyUpdateDto dto);
    Boolean delete(UUID id);
    String toggleActive(UUID id);
}
