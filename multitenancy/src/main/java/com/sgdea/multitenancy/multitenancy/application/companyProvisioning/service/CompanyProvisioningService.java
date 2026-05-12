package com.sgdea.multitenancy.multitenancy.application.companyProvisioning.service;

import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.application.company.service.CompanyService;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.service.CompanyDatabaseConnectionService;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.service.CompanyLicenseService;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto.CompanyProvisioningCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto.CompanyProvisioningDatabaseConnectionDto;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto.CompanyProvisioningLicenseDto;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto.CompanyProvisioningResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.usecase.CompanyProvisioningUseCase;
import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.service.CompanyUserService;
import com.sgdea.multitenancy.multitenancy.application.user.dto.UserResponseDto;
import com.sgdea.multitenancy.multitenancy.application.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyProvisioningService implements CompanyProvisioningUseCase {
    private final CompanyService companyService;
    private final UserService userService;
    private final CompanyUserService companyUserService;
    private final CompanyLicenseService companyLicenseService;
    private final CompanyDatabaseConnectionService companyDatabaseConnectionService;

    public CompanyProvisioningService(
            CompanyService companyService,
            UserService userService,
            CompanyUserService companyUserService,
            CompanyLicenseService companyLicenseService,
            CompanyDatabaseConnectionService companyDatabaseConnectionService) {
        this.companyService = companyService;
        this.userService = userService;
        this.companyUserService = companyUserService;
        this.companyLicenseService = companyLicenseService;
        this.companyDatabaseConnectionService = companyDatabaseConnectionService;
    }

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
