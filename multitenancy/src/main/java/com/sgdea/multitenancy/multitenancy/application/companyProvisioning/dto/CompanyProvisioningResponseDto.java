package com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.application.user.dto.UserResponseDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProvisioningResponseDto {
    private CompanyResponseDto company;
    private UserResponseDto adminUser;
    private CompanyUserResponseDto companyUser;
    private CompanyLicenseResponseDto license;
    private CompanyDatabaseConnectionResponseDto databaseConnection;
}
