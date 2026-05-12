package com.sgdea.multitenancy.multitenancy.application.companyProvisioning.usecase;

import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto.CompanyProvisioningCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto.CompanyProvisioningResponseDto;

public interface CompanyProvisioningUseCase {
    CompanyProvisioningResponseDto create(CompanyProvisioningCreateDto dto);
}
