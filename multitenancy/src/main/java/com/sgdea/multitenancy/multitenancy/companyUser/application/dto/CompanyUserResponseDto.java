package com.sgdea.multitenancy.multitenancy.companyUser.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserResponseDto {
    private Long id;
    private UUID companyId;
    private String companyName;
    private Long userId;
    private String userEmail;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Boolean active;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
