package com.sgdea.multitenancy.multitenancy.user.application.mapper;

import com.sgdea.multitenancy.multitenancy.user.application.dto.UserCreateDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserResponseDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserUpdateDto;
import com.sgdea.multitenancy.multitenancy.user.domain.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // ENTITY -> RESPONSE
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleCode", source = "role.code")
    @Mapping(target = "roleName", source = "role.name")
    UserResponseDto toResponseDTO(User entity);

    // CREATE DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "email", source = "email", qualifiedByName = "lower")
    User toEntity(UserCreateDto dto);

    // UPDATE DTO -> ENTITY (merge)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "email", source = "email", qualifiedByName = "lower")
    void updateEntityFromDTO(UserUpdateDto dto, @MappingTarget User entity);

    @Named("lower")
    default String lower(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
