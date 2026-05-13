package com.sgdea.multitenancy.multitenancy.companyLicense.application.mapper;

import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseCreateDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseUpdateDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.domain.model.CompanyLicense;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyLicenseMapper {

    // ENTITY -> RESPONSE
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "licenseTypeId", source = "licenseType.id")
    @Mapping(target = "licenseTypeName", source = "licenseType.name")
    CompanyLicenseResponseDto toResponseDTO(CompanyLicense entity);

    // CREATE DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "licenseType", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CompanyLicense toEntity(CompanyLicenseCreateDto dto);

    // UPDATE DTO -> ENTITY (merge)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "licenseType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(CompanyLicenseUpdateDto dto, @MappingTarget CompanyLicense entity);

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
