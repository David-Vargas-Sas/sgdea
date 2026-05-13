package com.sgdea.multitenancy.multitenancy.companyType.application.mapper;

import com.sgdea.multitenancy.multitenancy.companyType.application.dto.CompanyTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.companyType.application.dto.CompanyTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.companyType.application.dto.CompanyTypeUpdateDto;
import com.sgdea.multitenancy.multitenancy.companyType.domain.model.CompanyType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyTypeMapper {

    // ENTITY -> RESPONSE
    CompanyTypeResponseDto toResponseDTO(CompanyType entity);

    // CREATE DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "name", source = "name", qualifiedByName = "upper")
    CompanyType toEntity(CompanyTypeCreateDto dto);

    // UPDATE DTO -> ENTITY (merge)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "name", source = "name", qualifiedByName = "upper")
    void updateEntityFromDTO(CompanyTypeUpdateDto dto, @MappingTarget CompanyType entity);

    @Named("upper")
    default String upper(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
