package com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyCreateDto;
import com.sgdea.multitenancy.multitenancy.application.user.dto.UserCreateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProvisioningCreateDto {
    @Valid
    @NotNull(message = "Los datos de la empresa son obligatorios")
    private CompanyCreateDto company;

    @Valid
    @NotNull(message = "Los datos del usuario administrador son obligatorios")
    private UserCreateDto adminUser;

    @Valid
    @NotNull(message = "Los datos de la licencia son obligatorios")
    private CompanyProvisioningLicenseDto license;

    @Valid
    private CompanyProvisioningDatabaseConnectionDto databaseConnection;
}
