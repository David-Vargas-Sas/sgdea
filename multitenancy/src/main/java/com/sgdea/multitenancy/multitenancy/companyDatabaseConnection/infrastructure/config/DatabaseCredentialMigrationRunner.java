package com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.infrastructure.config;

import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.service.CompanyDatabaseConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "security.database-connection.migration",
        name = "encrypt-plaintext-on-startup",
        havingValue = "true",
        matchIfMissing = true)
public class DatabaseCredentialMigrationRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCredentialMigrationRunner.class);

    private final CompanyDatabaseConnectionService connectionService;

    public DatabaseCredentialMigrationRunner(CompanyDatabaseConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int updated = connectionService.encryptStoredPlaintextCredentials();
            if (updated > 0) {
                logger.info("Credenciales de conexiones de base de datos encriptadas: {}", updated);
            }
        } catch (Exception exception) {
            logger.warn("No se pudo ejecutar la migracion de credenciales de conexiones de base de datos: {}",
                    exception.getMessage());
        }
    }
}
