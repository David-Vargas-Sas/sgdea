package com.sgdea.multitenancy.multitenancy.application.companyType.mapper;

import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeUpdateDto;
import com.sgdea.multitenancy.multitenancy.domain.companyType.model.CompanyType;
import org.springframework.stereotype.Component;

@Component
public class CompanyTypeMapper {

    public CompanyType toEntity(CompanyTypeCreateDto dto) {
        CompanyType companyType = new CompanyType();
        companyType.setName(normalize(dto.getName()));
        companyType.setCreatedBy(trim(dto.getCreatedBy()));
        return companyType;
    }

    public void updateEntity(CompanyType companyType, CompanyTypeUpdateDto dto) {
        if (dto.getName() != null) {
            companyType.setName(normalize(dto.getName()));
        }
        if (dto.getUpdatedBy() != null) {
            companyType.setUpdatedBy(trim(dto.getUpdatedBy()));
        }
    }

    public CompanyTypeResponseDto toResponse(CompanyType companyType) {
        CompanyTypeResponseDto dto = new CompanyTypeResponseDto();
        dto.setId(companyType.getId());
        dto.setName(companyType.getName());
        dto.setCreatedAt(companyType.getCreatedAt());
        dto.setCreatedBy(companyType.getCreatedBy());
        dto.setUpdatedAt(companyType.getUpdatedAt());
        dto.setUpdatedBy(companyType.getUpdatedBy());
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
