package com.sgdea.multitenancy.multitenancy.application.companyLicense.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLicenseResponseDto {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private UUID licenseTypeId;
    private String licenseTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxUsers;
    private Boolean active;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
