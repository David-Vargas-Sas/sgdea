package com.sgdea.multitenancy.multitenancy.licenseType.application.usecase;

import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface LicenseTypeUseCase {
    List<LicenseTypeResponseDto> findAll();

    LicenseTypeResponseDto findById(UUID id);

    LicenseTypeResponseDto findByCode(String code);

    Page<LicenseTypeResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection);

    LicenseTypeResponseDto create(LicenseTypeCreateDto dto);

    LicenseTypeResponseDto update(UUID id, LicenseTypeUpdateDto dto);

    Boolean delete(UUID id);

    String toggleActive(UUID id);
}
