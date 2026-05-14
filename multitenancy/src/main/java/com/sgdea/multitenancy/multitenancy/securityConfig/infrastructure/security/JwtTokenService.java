package com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.model.CompanyDatabaseConnection;
import com.sgdea.multitenancy.multitenancy.user.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtTokenService {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String secret;
    private final String issuer;

    public JwtTokenService(
            ObjectMapper objectMapper,
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer:sgdea-multitenancy}") String issuer) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.issuer = issuer;
    }

    public String generateToken(User user, Company company, CompanyDatabaseConnection connection, LocalDateTime expiresAt) {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", issuer);
        payload.put("sub", user.getEmail());
        payload.put("userId", user.getId());
        payload.put("email", user.getEmail());
        payload.put("roleId", user.getRole().getId());
        payload.put("roleCode", user.getRole().getCode());
        payload.put("companyId", company.getId().toString());
        payload.put("companyCode", company.getCode());
        payload.put("connectionId", connection.getId().toString());
        payload.put("iat", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        payload.put("exp", expiresAt.toEpochSecond(ZoneOffset.UTC));

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;
        return unsignedToken + "." + sign(unsignedToken);
    }

    public Map<String, Object> validateAndGetClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token invalido");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new IllegalArgumentException("Token invalido");
        }

        Map<String, Object> claims = decodePayload(parts[1]);

        Object expiration = claims.get("exp");
        if (!(expiration instanceof Number) || ((Number) expiration).longValue() < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
            throw new IllegalArgumentException("Token expirado");
        }

        Object tokenIssuer = claims.get("iss");
        if (tokenIssuer == null || !issuer.equals(tokenIssuer.toString())) {
            throw new IllegalArgumentException("Token con issuer inválido");
        }

        return claims;
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible generar el token", exception);
        }
    }

    private Map<String, Object> decodePayload(String encodedPayload) {
        try {
            byte[] json = URL_DECODER.decode(encodedPayload);
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new IllegalArgumentException("Token invalido", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible firmar el token", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }

        int result = 0;
        for (int index = 0; index < leftBytes.length; index++) {
            result |= leftBytes[index] ^ rightBytes[index];
        }
        return result == 0;
    }
}
