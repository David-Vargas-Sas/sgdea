package com.sgdea.multitenancy.multitenancy.application.companyUser.mapper;

import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserUpdateDto;
import com.sgdea.multitenancy.multitenancy.domain.company.model.Company;
import com.sgdea.multitenancy.multitenancy.domain.companyUser.model.CompanyUser;
import com.sgdea.multitenancy.multitenancy.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class CompanyUserMapper {
    public CompanyUser toEntity(CompanyUserCreateDto dto, Company company, User user) {
        CompanyUser companyUser = new CompanyUser();
        companyUser.setCompany(company);
        companyUser.setUser(user);
        companyUser.setCreatedBy(trim(dto.getCreatedBy()));
        return companyUser;
    }

    public void updateEntity(CompanyUser companyUser, CompanyUserUpdateDto dto, Company company, User user) {
        if (dto.getCompanyId() != null) companyUser.setCompany(company);
        if (dto.getUserId() != null) companyUser.setUser(user);
        if (dto.getActive() != null) companyUser.setActive(dto.getActive());
        if (dto.getUpdatedBy() != null) companyUser.setUpdatedBy(trim(dto.getUpdatedBy()));
    }

    public CompanyUserResponseDto toResponse(CompanyUser companyUser) {
        CompanyUserResponseDto dto = new CompanyUserResponseDto();
        dto.setId(companyUser.getId());
        dto.setCompanyId(companyUser.getCompany().getId());
        dto.setCompanyName(companyUser.getCompany().getName());
        dto.setUserId(companyUser.getUser().getId());
        dto.setUserEmail(companyUser.getUser().getEmail());
        if (companyUser.getUser().getRole() != null) {
            dto.setRoleId(companyUser.getUser().getRole().getId());
            dto.setRoleCode(companyUser.getUser().getRole().getCode());
            dto.setRoleName(companyUser.getUser().getRole().getName());
        }
        dto.setActive(companyUser.getActive());
        dto.setCreatedAt(companyUser.getCreatedAt());
        dto.setCreatedBy(companyUser.getCreatedBy());
        dto.setUpdatedAt(companyUser.getUpdatedAt());
        dto.setUpdatedBy(companyUser.getUpdatedBy());
        return dto;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
