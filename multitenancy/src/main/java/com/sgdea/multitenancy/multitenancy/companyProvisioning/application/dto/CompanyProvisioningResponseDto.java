package com.sgdea.multitenancy.multitenancy.companyProvisioning.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserResponseDto;

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
