package com.sgdea.administracion.domain.entity.company;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
@NoArgsConstructor
@Getter
@Setter
public class Company {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column ( name = "code", nullable = false , unique = true)
    private String code;

    @Column ( name = "name", nullable = false)
    private String name;

    @Column(name = "nit", length = 20)
    private String nit;

    @Column(name = "dv", length = 1)
    private Integer dv;

    @Column(name = "path_logo")
    private String logoPath;

    @Column ( name = "active" , nullable = false)
    private Boolean active = true;

    @Column ( name = "created_at")
    private LocalDateTime createdAt;

    @Column ( name = "created_by")
    private String createdBy;

    @Column ( name = "updated_at")
    private LocalDateTime updatedAt;

    @Column ( name = "updated_by")
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
