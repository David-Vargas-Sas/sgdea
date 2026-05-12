package com.sgdea.multitenancy.multitenancy.application.licenseType.mapper;

import com.sgdea.multitenancy.multitenancy.application.licenseType.dto.LicenseTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.application.licenseType.dto.LicenseTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.application.licenseType.dto.LicenseTypeUpdateDto;
import com.sgdea.multitenancy.multitenancy.domain.licenseType.model.LicenseType;
import org.springframework.stereotype.Component;

@Component
public class LicenseTypeMapper {

    public LicenseType toEntity(LicenseTypeCreateDto dto) {
        LicenseType licenseType = new LicenseType();
        licenseType.setCode(normalize(dto.getCode()));
        licenseType.setName(trim(dto.getName()));
        licenseType.setDescription(trim(dto.getDescription()));
        return licenseType;
    }

    public void updateEntity(LicenseType licenseType, LicenseTypeUpdateDto dto) {
        if (dto.getCode() != null) {
            licenseType.setCode(normalize(dto.getCode()));
        }
        if (dto.getName() != null) {
            licenseType.setName(trim(dto.getName()));
        }
        if (dto.getDescription() != null) {
            licenseType.setDescription(trim(dto.getDescription()));
        }
        if (dto.getActive() != null) {
            licenseType.setActive(dto.getActive());
        }
    }

    public LicenseTypeResponseDto toResponse(LicenseType licenseType) {
        LicenseTypeResponseDto dto = new LicenseTypeResponseDto();
        dto.setId(licenseType.getId());
        dto.setCode(licenseType.getCode());
        dto.setName(licenseType.getName());
        dto.setDescription(licenseType.getDescription());
        dto.setActive(licenseType.getActive());
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
