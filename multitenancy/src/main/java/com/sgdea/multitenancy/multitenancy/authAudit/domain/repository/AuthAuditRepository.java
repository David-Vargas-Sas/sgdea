package com.sgdea.multitenancy.multitenancy.authAudit.domain.repository;

import com.sgdea.multitenancy.multitenancy.authAudit.domain.model.AuthAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthAuditRepository extends JpaRepository<AuthAudit, UUID> {
}
