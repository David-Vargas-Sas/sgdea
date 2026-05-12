package com.sgdea.multitenancy.multitenancy.application.companyLicense.mapper;

import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseUpdateDto;
import com.sgdea.multitenancy.multitenancy.domain.company.model.Company;
import com.sgdea.multitenancy.multitenancy.domain.companyLicense.model.CompanyLicense;
import com.sgdea.multitenancy.multitenancy.domain.licenseType.model.LicenseType;
import org.springframework.stereotype.Component;

@Component
public class CompanyLicenseMapper {
    public CompanyLicense toEntity(CompanyLicenseCreateDto dto, Company company, LicenseType licenseType) {
        CompanyLicense companyLicense = new CompanyLicense();
        companyLicense.setCompany(company);
        companyLicense.setLicenseType(licenseType);
        companyLicense.setStartDate(dto.getStartDate());
        companyLicense.setEndDate(dto.getEndDate());
        companyLicense.setMaxUsers(dto.getMaxUsers());
        companyLicense.setNotes(trim(dto.getNotes()));
        companyLicense.setCreatedBy(trim(dto.getCreatedBy()));
        return companyLicense;
    }

    public void updateEntity(CompanyLicense companyLicense, CompanyLicenseUpdateDto dto, Company company, LicenseType licenseType) {
        if (company != null) companyLicense.setCompany(company);
        if (licenseType != null) companyLicense.setLicenseType(licenseType);
        if (dto.getStartDate() != null) companyLicense.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) companyLicense.setEndDate(dto.getEndDate());
        if (dto.getMaxUsers() != null) companyLicense.setMaxUsers(dto.getMaxUsers());
        if (dto.getActive() != null) companyLicense.setActive(dto.getActive());
        if (dto.getNotes() != null) companyLicense.setNotes(trim(dto.getNotes()));
        if (dto.getUpdatedBy() != null) companyLicense.setUpdatedBy(trim(dto.getUpdatedBy()));
    }

    public CompanyLicenseResponseDto toResponse(CompanyLicense companyLicense) {
        CompanyLicenseResponseDto dto = new CompanyLicenseResponseDto();
        dto.setId(companyLicense.getId());
        dto.setCompanyId(companyLicense.getCompany().getId());
        dto.setCompanyName(companyLicense.getCompany().getName());
        dto.setLicenseTypeId(companyLicense.getLicenseType().getId());
        dto.setLicenseTypeName(companyLicense.getLicenseType().getName());
        dto.setStartDate(companyLicense.getStartDate());
        dto.setEndDate(companyLicense.getEndDate());
        dto.setMaxUsers(companyLicense.getMaxUsers());
        dto.setActive(companyLicense.getActive());
        dto.setNotes(companyLicense.getNotes());
        dto.setCreatedAt(companyLicense.getCreatedAt());
        dto.setCreatedBy(companyLicense.getCreatedBy());
        dto.setUpdatedAt(companyLicense.getUpdatedAt());
        dto.setUpdatedBy(companyLicense.getUpdatedBy());
        return dto;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
