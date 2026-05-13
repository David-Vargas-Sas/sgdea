package com.sgdea.multitenancy.multitenancy.companyUser.application.mapper;

import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserCreateDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserUpdateDto;
import com.sgdea.multitenancy.multitenancy.companyUser.domain.model.CompanyUser;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyUserMapper {

    // ENTITY -> RESPONSE
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "roleId", source = "user.role.id")
    @Mapping(target = "roleCode", source = "user.role.code")
    @Mapping(target = "roleName", source = "user.role.name")
    CompanyUserResponseDto toResponseDTO(CompanyUser entity);

    // CREATE DTO -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CompanyUser toEntity(CompanyUserCreateDto dto);

    // UPDATE DTO -> ENTITY (merge)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(CompanyUserUpdateDto dto, @MappingTarget CompanyUser entity);

    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
