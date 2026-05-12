package com.sgdea.multitenancy.multitenancy.domain.authAudit.repository;

import com.sgdea.multitenancy.multitenancy.domain.authAudit.model.AuthAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthAuditRepository extends JpaRepository<AuthAudit, UUID> {
}
