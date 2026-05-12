package com.sgdea.multitenancy.multitenancy.application.role.mapper;

import com.sgdea.multitenancy.multitenancy.application.role.dto.RoleCreateDto;
import com.sgdea.multitenancy.multitenancy.application.role.dto.RoleResponseDto;
import com.sgdea.multitenancy.multitenancy.application.role.dto.RoleUpdateDto;
import com.sgdea.multitenancy.multitenancy.domain.role.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {
    public Role toEntity(RoleCreateDto dto) {
        Role role = new Role();
        role.setCode(normalize(dto.getCode()));
        role.setName(trim(dto.getName()));
        role.setDescription(trim(dto.getDescription()));
        role.setCreatedBy(trim(dto.getCreatedBy()));
        return role;
    }

    public void updateEntity(Role role, RoleUpdateDto dto) {
        if (dto.getCode() != null) role.setCode(normalize(dto.getCode()));
        if (dto.getName() != null) role.setName(trim(dto.getName()));
        if (dto.getDescription() != null) role.setDescription(trim(dto.getDescription()));
        if (dto.getActive() != null) role.setActive(dto.getActive());
        if (dto.getUpdatedBy() != null) role.setUpdatedBy(trim(dto.getUpdatedBy()));
    }

    public RoleResponseDto toResponse(Role role) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setActive(role.getActive());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setCreatedBy(role.getCreatedBy());
        dto.setUpdatedAt(role.getUpdatedAt());
        dto.setUpdatedBy(role.getUpdatedBy());
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
