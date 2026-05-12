package com.sgdea.multitenancy.multitenancy.domain.companyType.repository;

import com.sgdea.multitenancy.multitenancy.domain.companyType.model.CompanyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyTypeRepository extends JpaRepository<CompanyType, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<CompanyType> findByNameIgnoreCase(String name);
}
