package com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.mapper;

import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionCreateDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionUpdateDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.model.CompanyDatabaseConnection;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyDatabaseConnectionMapper {

    // ENTITY -> RESPONSE
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    CompanyDatabaseConnectionResponseDto toResponseDTO(CompanyDatabaseConnection entity);

    // CREATE DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "provider", source = "provider", qualifiedByName = "upper")
    @Mapping(target = "defaultConnection", expression = "java(Boolean.TRUE.equals(dto.getDefaultConnection()))")
    CompanyDatabaseConnection toEntity(CompanyDatabaseConnectionCreateDto dto);

    // UPDATE DTO -> ENTITY (merge)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "provider", source = "provider", qualifiedByName = "upper")
    void updateEntityFromDTO(CompanyDatabaseConnectionUpdateDto dto, @MappingTarget CompanyDatabaseConnection entity);

    @Named("upper")
    default String upper(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
