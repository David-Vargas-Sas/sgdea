package com.sgdea.multitenancy.multitenancy.auth.domain.exceptions;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {
    private final HttpStatus status;

    private AuthException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static AuthException invalidCredentials() {
        return new AuthException("Correo o contrasena invalidos", HttpStatus.UNAUTHORIZED);
    }

    public static AuthException invalidToken(String message) {
        return new AuthException(message, HttpStatus.UNAUTHORIZED);
    }

    public static AuthException missingToken(String message) {
        return new AuthException(message, HttpStatus.BAD_REQUEST);
    }

    public static AuthException accessDenied(String message) {
        return new AuthException(message, HttpStatus.FORBIDDEN);
    }

    public static AuthException tenantConfiguration(String message) {
        return new AuthException(message, HttpStatus.CONFLICT);
    }

    public static AuthException tooManyAttempts(String message) {
        return new AuthException(message, HttpStatus.TOO_MANY_REQUESTS);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
