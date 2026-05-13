package com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class DatabaseCredentialEncryptionService {
    private static final String PREFIX = "v1:";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec secretKey;

    public DatabaseCredentialEncryptionService(
            @Value("${security.database-connection.encryption-secret}")
            String encryptionSecret) {
        if (encryptionSecret == null || encryptionSecret.isBlank()) {
            throw new IllegalArgumentException("La llave de encriptacion de credenciales de base de datos es obligatoria");
        }
        this.secretKey = new SecretKeySpec(sha256(encryptionSecret), ALGORITHM);
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public String encryptIfNeeded(String value) {
        if (value == null || value.isBlank() || isEncrypted(value)) {
            return value;
        }

        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            ByteBuffer payload = ByteBuffer.allocate(iv.length + encrypted.length);
            payload.put(iv);
            payload.put(encrypted);

            return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.array());
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible encriptar la credencial de base de datos", exception);
        }
    }

    public String decrypt(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        if (!isEncrypted(value)) {
            return value;
        }

        try {
            byte[] payload = Base64.getUrlDecoder().decode(value.substring(PREFIX.length()));
            if (payload.length <= IV_BYTES) {
                throw new IllegalArgumentException("Credencial encriptada invalida");
            }

            ByteBuffer buffer = ByteBuffer.wrap(payload);

            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalArgumentException("No fue posible desencriptar la credencial de base de datos", exception);
        }
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible preparar la llave de encriptacion", exception);
        }
    }
}
