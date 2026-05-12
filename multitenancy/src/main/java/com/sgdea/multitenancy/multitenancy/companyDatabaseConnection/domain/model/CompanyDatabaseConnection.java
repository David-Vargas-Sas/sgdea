package com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.model;

import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_database_connections")
@Getter
@Setter
@NoArgsConstructor
public class CompanyDatabaseConnection {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "connection_name", nullable = false, length = 100)
    private String connectionName;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "server", nullable = false, length = 255)
    private String server;

    @Column(name = "database_name", nullable = false, length = 255)
    private String databaseName;

    @Column(name = "port")
    private Integer port;

    @Column(name = "database_user", length = 255)
    private String databaseUser;

    @Column(name = "encrypted_password", length = 1000)
    private String encryptedPassword;

    @Column(name = "encrypted_connection_string", length = 2000)
    private String encryptedConnectionString;

    @Column(name = "default_connection", nullable = false)
    private Boolean defaultConnection = false;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.defaultConnection = Boolean.TRUE.equals(this.defaultConnection);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
