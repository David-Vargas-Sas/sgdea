package com.sgdea.administracion.multitenancy.application.mapper.user;

import com.sgdea.administracion.multitenancy.application.dto.user.UserCreateDto;
import com.sgdea.administracion.multitenancy.application.dto.user.UserResponseDto;
import com.sgdea.administracion.multitenancy.application.dto.user.UserUpdateDto;
import com.sgdea.administracion.multitenancy.domain.model.company.Company;
import com.sgdea.administracion.multitenancy.domain.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateDto dto, Company company, String passwordHash) {
        User user = new User();
        user.setCompany(company);
        user.setUsername(normalize(dto.getUsername()));
        user.setEmail(normalize(dto.getEmail()));
        user.setFullName(trim(dto.getFullName()));
        user.setPasswordHash(passwordHash);
        user.setRole(normalize(dto.getRole()));
        user.setCreatedBy(trim(dto.getCreatedBy()));
        return user;
    }

    public void updateEntity(User user, UserUpdateDto dto, Company company, String passwordHash) {
        if (dto.getCompanyId() != null) {
            user.setCompany(company);
        }
        if (dto.getUsername() != null) {
            user.setUsername(normalize(dto.getUsername()));
        }
        if (dto.getEmail() != null) {
            user.setEmail(normalize(dto.getEmail()));
        }
        if (dto.getFullName() != null) {
            user.setFullName(trim(dto.getFullName()));
        }
        if (passwordHash != null) {
            user.setPasswordHash(passwordHash);
        }
        if (dto.getRole() != null) {
            user.setRole(normalize(dto.getRole()));
        }
        if (dto.getActive() != null) {
            user.setActive(dto.getActive());
        }
        if (dto.getUpdatedBy() != null) {
            user.setUpdatedBy(trim(dto.getUpdatedBy()));
        }
    }

    public UserResponseDto toResponse(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        if (user.getCompany() != null) {
            dto.setCompanyId(user.getCompany().getId());
            dto.setCompanyName(user.getCompany().getName());
        }
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
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
