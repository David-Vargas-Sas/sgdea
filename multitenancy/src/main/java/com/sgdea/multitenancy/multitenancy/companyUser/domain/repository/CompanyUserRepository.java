package com.sgdea.multitenancy.multitenancy.companyUser.domain.repository;

import com.sgdea.multitenancy.multitenancy.companyUser.domain.model.CompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {
    boolean existsByCompanyIdAndUserId(UUID companyId, Long userId);

    boolean existsByCompanyIdAndUserIdAndIdNot(UUID companyId, Long userId, Long id);

    List<CompanyUser> findByCompanyId(UUID companyId);

    List<CompanyUser> findByUserId(Long userId);
}
