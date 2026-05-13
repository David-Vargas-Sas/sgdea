package com.sgdea.multitenancy.securityConfig.infrastructure.security;

import com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security.DatabaseCredentialEncryptionService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseCredentialEncryptionServiceTest {

    private final DatabaseCredentialEncryptionService service =
            new DatabaseCredentialEncryptionService("test-database-credential-secret");

    @Test
    void encryptIfNeededEncryptsPlaintextValue() {
        String encrypted = service.encryptIfNeeded("Tecs2026*");

        assertThat(encrypted).startsWith("v1:");
        assertThat(encrypted).doesNotContain("Tecs2026*");
        assertThat(service.decrypt(encrypted)).isEqualTo("Tecs2026*");
    }

    @Test
    void encryptIfNeededDoesNotEncryptAlreadyEncryptedValue() {
        String encrypted = service.encryptIfNeeded("Tecs2026*");

        assertThat(service.encryptIfNeeded(encrypted)).isEqualTo(encrypted);
    }

    @Test
    void decryptReturnsPlaintextValueWhenValueIsNotEncrypted() {
        assertThat(service.decrypt("Tecs2026*")).isEqualTo("Tecs2026*");
    }
}
