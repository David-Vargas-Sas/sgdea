package com.sgdea.multitenancy.multitenancy.domain.company.repository;

import com.sgdea.multitenancy.multitenancy.domain.company.model.Company;
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
