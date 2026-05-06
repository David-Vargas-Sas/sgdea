package com.sgdea.administracion.domain.mapper.company;

import com.sgdea.administracion.domain.dto.company.CompanyCreateDto;
import com.sgdea.administracion.domain.dto.company.CompanyResponseDto;
import com.sgdea.administracion.domain.dto.company.CompanyUpdateDto;
import com.sgdea.administracion.domain.entity.company.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public Company toEntity(CompanyCreateDto dto) {
        Company company = new Company();
        company.setCode(normalize(dto.getCode()));
        company.setName(trim(dto.getName()));
        company.setNit(trim(dto.getNit()));
        company.setDv(dto.getDv());
        company.setLogoPath(trim(dto.getLogoPath()));
        company.setCreatedBy(trim(dto.getCreatedBy()));
        return company;
    }

    public void updateEntity(Company company, CompanyUpdateDto dto) {
        if (dto.getCode() != null) {
            company.setCode(normalize(dto.getCode()));
        }
        if (dto.getName() != null) {
            company.setName(trim(dto.getName()));
        }
        if (dto.getNit() != null) {
            company.setNit(trim(dto.getNit()));
        }
        if (dto.getDv() != null) {
            company.setDv(dto.getDv());
        }
        if (dto.getLogoPath() != null) {
            company.setLogoPath(trim(dto.getLogoPath()));
        }
        if (dto.getActive() != null) {
            company.setActive(dto.getActive());
        }
        if (dto.getUpdatedBy() != null) {
            company.setUpdatedBy(trim(dto.getUpdatedBy()));
        }
    }

    public CompanyResponseDto toResponse(Company company) {
        CompanyResponseDto dto = new CompanyResponseDto();
        dto.setId(company.getId());
        dto.setCode(company.getCode());
        dto.setName(company.getName());
        dto.setNit(company.getNit());
        dto.setDv(company.getDv());
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
