package com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.mapper;

import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionUpdateDto;
import com.sgdea.multitenancy.multitenancy.domain.company.model.Company;
import com.sgdea.multitenancy.multitenancy.domain.companyDatabaseConnection.model.CompanyDatabaseConnection;
import org.springframework.stereotype.Component;

@Component
public class CompanyDatabaseConnectionMapper {
    public CompanyDatabaseConnection toEntity(CompanyDatabaseConnectionCreateDto dto, Company company) {
        CompanyDatabaseConnection connection = new CompanyDatabaseConnection();
        connection.setCompany(company);
        connection.setConnectionName(trim(dto.getConnectionName()));
        connection.setProvider(normalize(dto.getProvider()));
        connection.setServer(trim(dto.getServer()));
        connection.setDatabaseName(trim(dto.getDatabaseName()));
        connection.setPort(dto.getPort());
        connection.setDatabaseUser(trim(dto.getDatabaseUser()));
        connection.setEncryptedPassword(trim(dto.getEncryptedPassword()));
        connection.setEncryptedConnectionString(trim(dto.getEncryptedConnectionString()));
        connection.setDefaultConnection(Boolean.TRUE.equals(dto.getDefaultConnection()));
        connection.setCreatedBy(trim(dto.getCreatedBy()));
        return connection;
    }

    public void updateEntity(CompanyDatabaseConnection connection, CompanyDatabaseConnectionUpdateDto dto, Company company) {
        if (company != null) connection.setCompany(company);
        if (dto.getConnectionName() != null) connection.setConnectionName(trim(dto.getConnectionName()));
        if (dto.getProvider() != null) connection.setProvider(normalize(dto.getProvider()));
        if (dto.getServer() != null) connection.setServer(trim(dto.getServer()));
        if (dto.getDatabaseName() != null) connection.setDatabaseName(trim(dto.getDatabaseName()));
        if (dto.getPort() != null) connection.setPort(dto.getPort());
        if (dto.getDatabaseUser() != null) connection.setDatabaseUser(trim(dto.getDatabaseUser()));
        if (dto.getEncryptedPassword() != null) connection.setEncryptedPassword(trim(dto.getEncryptedPassword()));
        if (dto.getEncryptedConnectionString() != null) connection.setEncryptedConnectionString(trim(dto.getEncryptedConnectionString()));
        if (dto.getDefaultConnection() != null) connection.setDefaultConnection(dto.getDefaultConnection());
        if (dto.getActive() != null) connection.setActive(dto.getActive());
        if (dto.getUpdatedBy() != null) connection.setUpdatedBy(trim(dto.getUpdatedBy()));
    }

    public CompanyDatabaseConnectionResponseDto toResponse(CompanyDatabaseConnection connection) {
        CompanyDatabaseConnectionResponseDto dto = new CompanyDatabaseConnectionResponseDto();
        dto.setId(connection.getId());
        dto.setCompanyId(connection.getCompany().getId());
        dto.setCompanyName(connection.getCompany().getName());
        dto.setConnectionName(connection.getConnectionName());
        dto.setProvider(connection.getProvider());
        dto.setServer(connection.getServer());
        dto.setDatabaseName(connection.getDatabaseName());
        dto.setPort(connection.getPort());
        dto.setDatabaseUser(connection.getDatabaseUser());
        dto.setEncryptedPassword(connection.getEncryptedPassword());
        dto.setEncryptedConnectionString(connection.getEncryptedConnectionString());
        dto.setDefaultConnection(connection.getDefaultConnection());
        dto.setActive(connection.getActive());
        dto.setCreatedAt(connection.getCreatedAt());
        dto.setCreatedBy(connection.getCreatedBy());
        dto.setUpdatedAt(connection.getUpdatedAt());
        dto.setUpdatedBy(connection.getUpdatedBy());
        return dto;
    }

    private String normalize(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
