package com.sgdea.multitenancy.multitenancy.role.application.mapper;

import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleCreateDto;
import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleResponseDto;
import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleUpdateDto;
import com.sgdea.multitenancy.multitenancy.role.domain.model.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    // ENTITY -> RESPONSE
    RoleResponseDto toResponseDTO(Role entity);

    // CREATE DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", source = "code", qualifiedByName = "upper")
    Role toEntity(RoleCreateDto dto);

    // UPDATE DTO -> ENTITY (merge)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "code", source = "code", qualifiedByName = "upper")
    void updateEntityFromDTO(RoleUpdateDto dto, @MappingTarget Role entity);

    @Named("upper")
    default String upper(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
