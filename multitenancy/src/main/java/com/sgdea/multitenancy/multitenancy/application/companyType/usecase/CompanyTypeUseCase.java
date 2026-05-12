package com.sgdea.multitenancy.multitenancy.application.companyType.usecase;

import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CompanyTypeUseCase {
    List<CompanyTypeResponseDto> findAll();

    CompanyTypeResponseDto findById(Long id);

    CompanyTypeResponseDto findByName(String name);

    Page<CompanyTypeResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection);

    CompanyTypeResponseDto create(CompanyTypeCreateDto dto);

    CompanyTypeResponseDto update(Long id, CompanyTypeUpdateDto dto);

    Boolean delete(Long id);
}
