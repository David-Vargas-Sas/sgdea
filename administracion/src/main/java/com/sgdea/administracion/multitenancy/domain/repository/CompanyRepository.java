package com.sgdea.administracion.multitenancy.domain.repository;

import com.sgdea.administracion.multitenancy.domain.model.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    Optional<Company> findByCodeIgnoreCase(String code);
}
