package com.sgdea.multitenancy.multitenancy.companyLicense.application.usecase;

import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseCreateDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CompanyLicenseUseCase {
    List<CompanyLicenseResponseDto> findAll();
    CompanyLicenseResponseDto findById(UUID id);
    List<CompanyLicenseResponseDto> findByCompanyId(UUID companyId);
    List<CompanyLicenseResponseDto> findByLicenseTypeId(UUID licenseTypeId);
    Page<CompanyLicenseResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection);
    CompanyLicenseResponseDto create(CompanyLicenseCreateDto dto);
    CompanyLicenseResponseDto update(UUID id, CompanyLicenseUpdateDto dto);
    Boolean delete(UUID id);
    String toggleActive(UUID id);
}
