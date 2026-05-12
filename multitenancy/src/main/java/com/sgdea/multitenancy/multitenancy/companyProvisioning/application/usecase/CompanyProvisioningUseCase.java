package com.sgdea.multitenancy.multitenancy.companyProvisioning.application.usecase;

import com.sgdea.multitenancy.multitenancy.companyProvisioning.application.dto.CompanyProvisioningCreateDto;
import com.sgdea.multitenancy.multitenancy.companyProvisioning.application.dto.CompanyProvisioningResponseDto;

public interface CompanyProvisioningUseCase {
    CompanyProvisioningResponseDto create(CompanyProvisioningCreateDto dto);
}
