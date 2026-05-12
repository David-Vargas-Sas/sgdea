package com.sgdea.apigateway.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Configuración del decodificador JWT del Gateway.
 *
 * <p>Valida los tokens HMAC-SHA256 emitidos por el microservicio <em>multitenancy</em>
 * usando el <strong>secreto compartido</strong> {@code security.jwt.secret}.
 * Esto elimina la dependencia de un servidor de autorización externo (OIDC/JWKS)
 * y permite validar los tokens de forma local y eficiente.
 *
 * <p><b>Importante</b>: {@code security.jwt.secret} debe ser idéntico entre el
 * Gateway y multitenancy. Configúralo a través de la variable de entorno
 * {@code SECURITY_JWT_SECRET} en producción (K8s Secret, Docker env, etc).
 */
@Configuration
public class GatewayJwtConfig {

    /**
     * Bean de decodificación JWT con secreto HMAC-SHA256 compartido.
     * Valida firma, emisor (iss) y expiración (exp).
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer:sgdea-multitenancy}") String issuer) {

        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Combinar validadores: emisor + timestamp (exp/nbf)
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(issuer);
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(issuerValidator, timestampValidator);

        decoder.setJwtValidator(combinedValidator);
        return decoder;
    }

    /**
     * Converter que extrae el claim {@code roleCode} del JWT y lo convierte
     * en un {@code GrantedAuthority} con prefijo {@code ROLE_}.
     * El principal del contexto de seguridad usará el claim {@code email}.
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Extraer ROLE_ desde el claim "roleCode" emitido por multitenancy
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String roleCode = jwt.getClaimAsString("roleCode");
            if (roleCode == null || roleCode.isBlank()) {
                return Collections.emptyList();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
        });

        // Usar "email" como identificador principal en lugar del "sub" estándar
        converter.setPrincipalClaimName("email");

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}

