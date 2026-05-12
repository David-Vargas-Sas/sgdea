package com.sgdea.multitenancy.multitenancy.application.companyLicense.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLicenseUpdateDto {
    private UUID companyId;
    private UUID licenseTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxUsers;
    private Boolean active;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notes;

    @Size(max = 100, message = "El usuario actualizador no puede superar 100 caracteres")
    private String updatedBy;
}
