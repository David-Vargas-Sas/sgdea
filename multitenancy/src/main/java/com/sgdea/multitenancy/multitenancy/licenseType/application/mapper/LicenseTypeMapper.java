package com.sgdea.multitenancy.multitenancy.licenseType.application.mapper;

import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeUpdateDto;
import com.sgdea.multitenancy.multitenancy.licenseType.domain.model.LicenseType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface LicenseTypeMapper {

    // ENTITY -> RESPONSE
    LicenseTypeResponseDto toResponseDTO(LicenseType entity);

    // CREATE DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "code", source = "code", qualifiedByName = "upper")
    LicenseType toEntity(LicenseTypeCreateDto dto);

    // UPDATE DTO -> ENTITY (merge)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "code", qualifiedByName = "upper")
    void updateEntityFromDTO(LicenseTypeUpdateDto dto, @MappingTarget LicenseType entity);

    @Named("upper")
    default String upper(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
