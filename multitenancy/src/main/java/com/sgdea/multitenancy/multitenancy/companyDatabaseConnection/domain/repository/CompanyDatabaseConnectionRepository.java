package com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.repository;

import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.model.CompanyDatabaseConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyDatabaseConnectionRepository extends JpaRepository<CompanyDatabaseConnection, UUID> {
    boolean existsByCompanyIdAndConnectionNameIgnoreCase(UUID companyId, String connectionName);

    boolean existsByCompanyIdAndConnectionNameIgnoreCaseAndIdNot(UUID companyId, String connectionName, UUID id);

    List<CompanyDatabaseConnection> findByCompanyId(UUID companyId);

    List<CompanyDatabaseConnection> findByCompanyIdAndDefaultConnectionTrue(UUID companyId);
}
