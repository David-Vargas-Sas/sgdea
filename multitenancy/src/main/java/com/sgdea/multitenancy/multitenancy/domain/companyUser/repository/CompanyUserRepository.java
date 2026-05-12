package com.sgdea.multitenancy.multitenancy.domain.companyUser.repository;

import com.sgdea.multitenancy.multitenancy.domain.companyUser.model.CompanyUser;
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
