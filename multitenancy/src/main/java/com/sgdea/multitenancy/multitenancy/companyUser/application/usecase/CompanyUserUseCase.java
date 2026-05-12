package com.sgdea.multitenancy.multitenancy.companyUser.application.usecase;

import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserCreateDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CompanyUserUseCase {
    List<CompanyUserResponseDto> findAll();
    CompanyUserResponseDto findById(Long id);
    List<CompanyUserResponseDto> findByCompanyId(UUID companyId);
    List<CompanyUserResponseDto> findByUserId(Long userId);
    Page<CompanyUserResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection);
    CompanyUserResponseDto create(CompanyUserCreateDto dto);
    CompanyUserResponseDto update(Long id, CompanyUserUpdateDto dto);
    Boolean delete(Long id);
    String toggleActive(Long id);
}
