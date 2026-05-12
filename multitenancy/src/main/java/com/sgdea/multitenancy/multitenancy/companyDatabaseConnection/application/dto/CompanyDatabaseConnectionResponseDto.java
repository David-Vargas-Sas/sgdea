package com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto;


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
public class CompanyDatabaseConnectionResponseDto {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private String connectionName;
    private String provider;
    private String server;
    private String databaseName;
    private Integer port;
    private String databaseUser;
    private String encryptedPassword;
    private String encryptedConnectionString;
    private Boolean defaultConnection;
    private Boolean active;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
