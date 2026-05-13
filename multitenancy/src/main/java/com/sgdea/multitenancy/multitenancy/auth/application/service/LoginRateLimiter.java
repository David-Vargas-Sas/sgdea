package com.sgdea.multitenancy.multitenancy.auth.application.service;

import com.sgdea.multitenancy.multitenancy.auth.domain.exceptions.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginRateLimiter {
    private final ConcurrentMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration lockDuration;

    public LoginRateLimiter(
            @Value("${security.login.max-failed-attempts:5}") int maxAttempts,
            @Value("${security.login.lock-minutes:15}") long lockMinutes) {
        this.maxAttempts = maxAttempts;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
    }

    public void assertAllowed(String email) {
        LoginAttempt attempt = attempts.get(key(email));
        if (attempt == null || attempt.lockedUntil == null || Instant.now().isAfter(attempt.lockedUntil)) {
            return;
        }
        throw AuthException.tooManyAttempts("Demasiados intentos fallidos. Intente nuevamente mas tarde");
    }

    public void recordFailure(String email) {
        attempts.compute(key(email), (key, current) -> {
            LoginAttempt attempt = current == null ? new LoginAttempt() : current;
            attempt.failedAttempts++;
            if (attempt.failedAttempts >= maxAttempts) {
                attempt.lockedUntil = Instant.now().plus(lockDuration);
            }
            return attempt;
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(key(email));
    }

    private String key(String email) {
        return normalize(email) + "|" + getIpAddress();
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String getIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "unknown";
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private HttpServletRequest getCurrentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private static class LoginAttempt {
        private int failedAttempts;
        private Instant lockedUntil;
    }
}
