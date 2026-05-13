package com.sgdea.multitenancy.multitenancy.company.application.mapper;

import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyCreateDto;
import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyUpdateDto;
import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    // ENTITY -> RESPONSE
    @Mapping(target = "companyTypeId", source = "companyType.id")
    @Mapping(target = "companyTypeName", source = "companyType.name")
    CompanyResponseDto toResponseDTO(Company entity);

    // CREATE DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyType", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", source = "code", qualifiedByName = "upper")
    Company toEntity(CompanyCreateDto dto);

    // UPDATE DTO -> ENTITY (merge)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "code", source = "code", qualifiedByName = "upper")
    void updateEntityFromDTO(CompanyUpdateDto dto, @MappingTarget Company entity);

    @Named("upper")
    default String upper(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
