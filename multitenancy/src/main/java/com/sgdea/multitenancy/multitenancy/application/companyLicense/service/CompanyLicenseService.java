package com.sgdea.multitenancy.multitenancy.application.companyLicense.service;

import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.mapper.CompanyLicenseMapper;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.usecase.CompanyLicenseUseCase;
import com.sgdea.multitenancy.multitenancy.domain.company.model.Company;
import com.sgdea.multitenancy.multitenancy.domain.company.repository.CompanyRepository;
import com.sgdea.multitenancy.multitenancy.domain.companyLicense.model.CompanyLicense;
import com.sgdea.multitenancy.multitenancy.domain.companyLicense.repository.CompanyLicenseRepository;
import com.sgdea.multitenancy.multitenancy.domain.licenseType.model.LicenseType;
import com.sgdea.multitenancy.multitenancy.domain.licenseType.repository.LicenseTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyLicenseService implements CompanyLicenseUseCase {
    private final CompanyLicenseRepository repository;
    private final CompanyRepository companyRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final CompanyLicenseMapper mapper;

    public CompanyLicenseService(CompanyLicenseRepository repository, CompanyRepository companyRepository,
                                 LicenseTypeRepository licenseTypeRepository, CompanyLicenseMapper mapper) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyLicenseResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyLicenseResponseDto findById(UUID id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyLicenseResponseDto> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyLicenseResponseDto> findByLicenseTypeId(UUID licenseTypeId) {
        return repository.findByLicenseTypeId(licenseTypeId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyLicenseResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy))).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CompanyLicenseResponseDto create(CompanyLicenseCreateDto dto) {
        Company company = getCompany(dto.getCompanyId());
        LicenseType licenseType = getLicenseType(dto.getLicenseTypeId());
        return mapper.toResponse(repository.save(mapper.toEntity(dto, company, licenseType)));
    }

    @Override
    @Transactional
    public CompanyLicenseResponseDto update(UUID id, CompanyLicenseUpdateDto dto) {
        CompanyLicense companyLicense = getEntityById(id);
        Company company = dto.getCompanyId() == null ? null : getCompany(dto.getCompanyId());
        LicenseType licenseType = dto.getLicenseTypeId() == null ? null : getLicenseType(dto.getLicenseTypeId());
        mapper.updateEntity(companyLicense, dto, company, licenseType);
        return mapper.toResponse(repository.save(companyLicense));
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
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe una licencia de empresa con id " + id));
    }

    private Company getCompany(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe una empresa con id " + id));
    }

    private LicenseType getLicenseType(UUID id) {
        return licenseTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe un tipo de licencia con id " + id));
    }
}
