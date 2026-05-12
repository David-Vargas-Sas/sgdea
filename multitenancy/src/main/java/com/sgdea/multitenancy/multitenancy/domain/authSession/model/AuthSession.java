package com.sgdea.multitenancy.multitenancy.domain.authSession.model;

import com.sgdea.multitenancy.multitenancy.domain.company.model.Company;
import com.sgdea.multitenancy.multitenancy.domain.companyDatabaseConnection.model.CompanyDatabaseConnection;
import com.sgdea.multitenancy.multitenancy.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions")
@Getter
@Setter
@NoArgsConstructor
public class AuthSession {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(name = "id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 1000)
    private String token;

    @Column(name = "refresh_token", nullable = false, unique = true, length = 1000)
    private String refreshToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false)
    private CompanyDatabaseConnection connection;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "refresh_expires_at", nullable = false)
    private LocalDateTime refreshExpiresAt;

    @Column(name = "logged_out_at")
    private LocalDateTime loggedOutAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }
}
