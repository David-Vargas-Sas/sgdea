package com.sgdea.multitenancy.multitenancy.application.company.dto;


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
public class CompanyResponseDto {
    private UUID id;
    private Long companyTypeId;
    private String companyTypeName;
    private String code;
    private String name;
    private String taxId;
    private String verificationDigit;
    private String logoPath;
    private Boolean active;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
