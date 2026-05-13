package com.sgdea.multitenancy.multitenancy.authAudit.application.service;

import com.sgdea.multitenancy.multitenancy.authAudit.application.dto.AuthAuditResponseDto;
import com.sgdea.multitenancy.multitenancy.authAudit.application.mapper.AuthAuditMapper;
import com.sgdea.multitenancy.multitenancy.authAudit.application.usecase.AuthAuditUseCase;
import com.sgdea.multitenancy.multitenancy.authAudit.domain.model.AuthAudit;
import com.sgdea.multitenancy.multitenancy.authAudit.domain.repository.AuthAuditRepository;
import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import com.sgdea.multitenancy.multitenancy.user.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuthAuditService implements AuthAuditUseCase {
    private final AuthAuditRepository repository;
    private final AuthAuditMapper mapper;

    public AuthAuditService(AuthAuditRepository repository, AuthAuditMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthAuditResponseDto> findAllPaginated(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(pageRequest).map(mapper::toResponseDTO);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String eventType, String message, User user, Company company) {
        record(eventType, true, message, user != null ? user.getEmail() : null, user, company);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String eventType, String message, String email) {
        record(eventType, false, message, email, null, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String eventType, String message, String email, User user, Company company) {
        record(eventType, false, message, email, user, company);
    }

    private void record(String eventType, Boolean success, String message, String email, User user, Company company) {
        AuthAudit audit = new AuthAudit();
        audit.setEventType(eventType);
        audit.setSuccess(success);
        audit.setMessage(limit(message, 255));
        audit.setEmail(limit(email, 150));
        audit.setUser(user);
        audit.setCompany(company);
        audit.setIpAddress(limit(getIpAddress(), 100));
        audit.setUserAgent(limit(getUserAgent(), 500));
        repository.save(audit);
    }

    private String getIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private HttpServletRequest getCurrentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
