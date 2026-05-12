package com.sgdea.multitenancy.multitenancy.companyProvisioning.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProvisioningLicenseDto {
    @NotNull(message = "El tipo de licencia es obligatorio")
    private UUID licenseTypeId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    private LocalDate endDate;
    private Integer maxUsers;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notes;
}
