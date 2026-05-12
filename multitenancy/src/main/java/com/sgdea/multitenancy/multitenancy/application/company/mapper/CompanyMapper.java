package com.sgdea.multitenancy.multitenancy.application.company.mapper;

import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyCreateDto;
import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyUpdateDto;
import com.sgdea.multitenancy.multitenancy.domain.company.model.Company;
import com.sgdea.multitenancy.multitenancy.domain.companyType.model.CompanyType;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {
    public Company toEntity(CompanyCreateDto dto, CompanyType companyType) {
        Company company = new Company();
        company.setCompanyType(companyType);
        company.setCode(normalize(dto.getCode()));
        company.setName(trim(dto.getName()));
        company.setTaxId(trim(dto.getTaxId()));
        company.setVerificationDigit(trim(dto.getVerificationDigit()));
        company.setLogoPath(trim(dto.getLogoPath()));
        company.setCreatedBy(trim(dto.getCreatedBy()));
        return company;
    }

    public void updateEntity(Company company, CompanyUpdateDto dto, CompanyType companyType) {
        if (dto.getCompanyTypeId() != null) company.setCompanyType(companyType);
        if (dto.getCode() != null) company.setCode(normalize(dto.getCode()));
        if (dto.getName() != null) company.setName(trim(dto.getName()));
        if (dto.getTaxId() != null) company.setTaxId(trim(dto.getTaxId()));
        if (dto.getVerificationDigit() != null) company.setVerificationDigit(trim(dto.getVerificationDigit()));
        if (dto.getLogoPath() != null) company.setLogoPath(trim(dto.getLogoPath()));
        if (dto.getActive() != null) company.setActive(dto.getActive());
        if (dto.getUpdatedBy() != null) company.setUpdatedBy(trim(dto.getUpdatedBy()));
    }

    public CompanyResponseDto toResponse(Company company) {
        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setId(company.getId());
        if (company.getCompanyType() != null) {
            dto.setCompanyTypeId(company.getCompanyType().getId());
            dto.setCompanyTypeName(company.getCompanyType().getName());
        }
        dto.setCode(company.getCode());
        dto.setName(company.getName());
        dto.setTaxId(company.getTaxId());
        dto.setVerificationDigit(company.getVerificationDigit());
        dto.setLogoPath(company.getLogoPath());
        dto.setActive(company.getActive());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setCreatedBy(company.getCreatedBy());
        dto.setUpdatedAt(company.getUpdatedAt());
        dto.setUpdatedBy(company.getUpdatedBy());
        return dto;
    }

    private String normalize(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
