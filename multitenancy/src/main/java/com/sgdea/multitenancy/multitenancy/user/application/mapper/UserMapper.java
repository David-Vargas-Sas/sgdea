package com.sgdea.multitenancy.multitenancy.user.application.mapper;

import com.sgdea.multitenancy.multitenancy.user.application.dto.UserCreateDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserResponseDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserUpdateDto;
import com.sgdea.multitenancy.multitenancy.role.domain.model.Role;
import com.sgdea.multitenancy.multitenancy.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserCreateDto dto, String passwordHash, Role role) {
        User user = new User();
        user.setDocumentNumber(trim(dto.getDocumentNumber()));
        user.setEmail(normalize(dto.getEmail()));
        user.setPasswordHash(passwordHash);
        user.setFirstName(trim(dto.getFirstName()));
        user.setSecondName(trim(dto.getSecondName()));
        user.setFirstLastName(trim(dto.getFirstLastName()));
        user.setSecondLastName(trim(dto.getSecondLastName()));
        user.setPhone(trim(dto.getPhone()));
        user.setRole(role);
        user.setCreatedBy(trim(dto.getCreatedBy()));
        return user;
    }

    public void updateEntity(User user, UserUpdateDto dto, String passwordHash, Role role) {
        if (dto.getDocumentNumber() != null) user.setDocumentNumber(trim(dto.getDocumentNumber()));
        if (dto.getEmail() != null) user.setEmail(normalize(dto.getEmail()));
        if (passwordHash != null) user.setPasswordHash(passwordHash);
        if (dto.getFirstName() != null) user.setFirstName(trim(dto.getFirstName()));
        if (dto.getSecondName() != null) user.setSecondName(trim(dto.getSecondName()));
        if (dto.getFirstLastName() != null) user.setFirstLastName(trim(dto.getFirstLastName()));
        if (dto.getSecondLastName() != null) user.setSecondLastName(trim(dto.getSecondLastName()));
        if (dto.getPhone() != null) user.setPhone(trim(dto.getPhone()));
        if (dto.getRoleId() != null) user.setRole(role);
        if (dto.getActive() != null) user.setActive(dto.getActive());
        if (dto.getUpdatedBy() != null) user.setUpdatedBy(trim(dto.getUpdatedBy()));
    }

    public UserResponseDto toResponse(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setDocumentNumber(user.getDocumentNumber());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setSecondName(user.getSecondName());
        dto.setFirstLastName(user.getFirstLastName());
        dto.setSecondLastName(user.getSecondLastName());
        dto.setPhone(user.getPhone());
        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleCode(user.getRole().getCode());
            dto.setRoleName(user.getRole().getName());
        }
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setUpdatedBy(user.getUpdatedBy());
        return dto;
    }

    private String normalize(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
