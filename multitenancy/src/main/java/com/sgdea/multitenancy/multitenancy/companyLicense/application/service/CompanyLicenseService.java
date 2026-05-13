package com.sgdea.multitenancy.multitenancy.companyLicense.application.service;

import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseCreateDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.dto.CompanyLicenseUpdateDto;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.mapper.CompanyLicenseMapper;
import com.sgdea.multitenancy.multitenancy.companyLicense.application.usecase.CompanyLicenseUseCase;
import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import com.sgdea.multitenancy.multitenancy.company.domain.repository.CompanyRepository;
import com.sgdea.multitenancy.multitenancy.companyLicense.domain.model.CompanyLicense;
import com.sgdea.multitenancy.multitenancy.companyLicense.domain.repository.CompanyLicenseRepository;
import com.sgdea.multitenancy.multitenancy.licenseType.domain.model.LicenseType;
import com.sgdea.multitenancy.multitenancy.licenseType.domain.repository.LicenseTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CompanyLicenseService implements CompanyLicenseUseCase {
    private final CompanyLicenseRepository repository;
    private final CompanyRepository companyRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final CompanyLicenseMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public List<CompanyLicenseResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyLicenseResponseDto findById(UUID id) {
        return mapper.toResponseDTO(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyLicenseResponseDto> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).stream().map(mapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyLicenseResponseDto> findByLicenseTypeId(UUID licenseTypeId) {
        return repository.findByLicenseTypeId(licenseTypeId).stream().map(mapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyLicenseResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy))).map(mapper::toResponseDTO);
    }

    @Override
    @Transactional
    public CompanyLicenseResponseDto create(CompanyLicenseCreateDto dto) {
        Company company = getCompany(dto.getCompanyId());
        LicenseType licenseType = getLicenseType(dto.getLicenseTypeId());
        CompanyLicense companyLicense = mapper.toEntity(dto);
        companyLicense.setCompany(company);
        companyLicense.setLicenseType(licenseType);
        return mapper.toResponseDTO(repository.save(companyLicense));
    }

    @Override
    @Transactional
    public CompanyLicenseResponseDto update(UUID id, CompanyLicenseUpdateDto dto) {
        CompanyLicense companyLicense = getEntityById(id);
        Company company = dto.getCompanyId() == null ? null : getCompany(dto.getCompanyId());
        LicenseType licenseType = dto.getLicenseTypeId() == null ? null : getLicenseType(dto.getLicenseTypeId());
        mapper.updateEntityFromDTO(dto, companyLicense);
        if (company != null) {
            companyLicense.setCompany(company);
        }
        if (licenseType != null) {
            companyLicense.setLicenseType(licenseType);
        }
        return mapper.toResponseDTO(repository.save(companyLicense));
    }

    @Override
    @Transactional
    public Boolean delete(UUID id) {
        repository.delete(getEntityById(id));
        return true;
    }

    @Override
    @Transactional
    public String toggleActive(UUID id) {
        CompanyLicense companyLicense = getEntityById(id);
        companyLicense.setActive(!Boolean.TRUE.equals(companyLicense.getActive()));
        repository.save(companyLicense);
        return companyLicense.getActive() ? "Licencia de empresa activada correctamente" : "Licencia de empresa desactivada correctamente";
    }

    private CompanyLicense getEntityById(UUID id) {
        return repository.getReferenceById(id);
    }

    private Company getCompany(UUID id) {
        return companyRepository.getReferenceById(id);
    }

    private LicenseType getLicenseType(UUID id) {
        return licenseTypeRepository.getReferenceById(id);
    }
}
