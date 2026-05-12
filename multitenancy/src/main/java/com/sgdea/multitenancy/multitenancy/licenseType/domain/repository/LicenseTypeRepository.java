package com.sgdea.multitenancy.multitenancy.licenseType.domain.repository;

import com.sgdea.multitenancy.multitenancy.licenseType.domain.model.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LicenseTypeRepository extends JpaRepository<LicenseType, UUID> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    Optional<LicenseType> findByCodeIgnoreCase(String code);
}
