package com.sgdea.multitenancy.multitenancy.companyLicense.domain.repository;

import com.sgdea.multitenancy.multitenancy.companyLicense.domain.model.CompanyLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyLicenseRepository extends JpaRepository<CompanyLicense, UUID> {
    List<CompanyLicense> findByCompanyId(UUID companyId);

    List<CompanyLicense> findByLicenseTypeId(UUID licenseTypeId);
}
