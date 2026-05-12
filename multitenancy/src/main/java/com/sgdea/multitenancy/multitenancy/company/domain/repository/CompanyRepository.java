package com.sgdea.multitenancy.multitenancy.company.domain.repository;

import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    Optional<Company> findByCodeIgnoreCase(String code);
}
