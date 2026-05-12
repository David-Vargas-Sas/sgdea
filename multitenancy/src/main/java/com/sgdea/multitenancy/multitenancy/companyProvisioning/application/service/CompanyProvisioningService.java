package com.sgdea.multitenancy.multitenancy.companyProvisioning.application.service;

import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.company.application.service.CompanyService;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionCreateDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.service.CompanyDatabaseConnectionService;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseCreateDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.service.CompanyLicenseService;
import com.sgdea.multitenancy.multitenancy.companyProvisioning.application.dto.CompanyProvisioningCreateDto;
import com.sgdea.multitenancy.multitenancy.companyProvisioning.application.dto.CompanyProvisioningDatabaseConnectionDto;
import com.sgdea.multitenancy.multitenancy.companyProvisioning.application.dto.CompanyProvisioningLicenseDto;
import com.sgdea.multitenancy.multitenancy.companyProvisioning.application.dto.CompanyProvisioningResponseDto;
import com.sgdea.multitenancy.multitenancy.companyProvisioning.application.usecase.CompanyProvisioningUseCase;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserCreateDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.service.CompanyUserService;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserResponseDto;
import com.sgdea.multitenancy.multitenancy.user.application.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CompanyProvisioningService implements CompanyProvisioningUseCase {
    private final CompanyService companyService;
    private final UserService userService;
    private final CompanyUserService companyUserService;
    private final CompanyLicenseService companyLicenseService;
    private final CompanyDatabaseConnectionService companyDatabaseConnectionService;


    @Override
    @Transactional
    public CompanyProvisioningResponseDto create(CompanyProvisioningCreateDto dto) {
        CompanyResponseDto company = companyService.create(dto.getCompany());
        UserResponseDto adminUser = userService.create(dto.getAdminUser());
        CompanyUserResponseDto companyUser = companyUserService.create(buildCompanyUser(dto, company, adminUser));
        CompanyLicenseResponseDto license = createLicense(dto, company);
        CompanyDatabaseConnectionResponseDto databaseConnection = createDatabaseConnection(dto, company);

        CompanyProvisioningResponseDto response = new CompanyProvisioningResponseDto();
        response.setCompany(company);
        response.setAdminUser(adminUser);
        response.setCompanyUser(companyUser);
        response.setLicense(license);
        response.setDatabaseConnection(databaseConnection);
        return response;
    }

    private CompanyUserCreateDto buildCompanyUser(
            CompanyProvisioningCreateDto dto,
            CompanyResponseDto company,
            UserResponseDto adminUser) {
        CompanyUserCreateDto companyUser = new CompanyUserCreateDto();
        companyUser.setCompanyId(company.getId());
        companyUser.setUserId(adminUser.getId());
        companyUser.setCreatedBy(dto.getCompany().getCreatedBy());
        return companyUser;
    }

    private CompanyLicenseResponseDto createLicense(CompanyProvisioningCreateDto dto, CompanyResponseDto company) {
        CompanyProvisioningLicenseDto source = dto.getLicense();
        CompanyLicenseCreateDto license = new CompanyLicenseCreateDto();
        license.setCompanyId(company.getId());
        license.setLicenseTypeId(source.getLicenseTypeId());
        license.setStartDate(source.getStartDate());
        license.setEndDate(source.getEndDate());
        license.setMaxUsers(source.getMaxUsers());
        license.setNotes(source.getNotes());
        license.setCreatedBy(dto.getCompany().getCreatedBy());
        return companyLicenseService.create(license);
    }

    private CompanyDatabaseConnectionResponseDto createDatabaseConnection(
            CompanyProvisioningCreateDto dto,
            CompanyResponseDto company) {
        if (dto.getDatabaseConnection() == null) {
            return null;
        }

        CompanyProvisioningDatabaseConnectionDto source = dto.getDatabaseConnection();
        CompanyDatabaseConnectionCreateDto connection = new CompanyDatabaseConnectionCreateDto();
        connection.setCompanyId(company.getId());
        connection.setConnectionName(source.getConnectionName());
        connection.setProvider(source.getProvider());
        connection.setServer(source.getServer());
        connection.setDatabaseName(source.getDatabaseName());
        connection.setPort(source.getPort());
        connection.setDatabaseUser(source.getDatabaseUser());
        connection.setEncryptedPassword(source.getEncryptedPassword());
        connection.setEncryptedConnectionString(source.getEncryptedConnectionString());
        connection.setDefaultConnection(source.getDefaultConnection());
        connection.setCreatedBy(dto.getCompany().getCreatedBy());
        return companyDatabaseConnectionService.create(connection);
    }
}
