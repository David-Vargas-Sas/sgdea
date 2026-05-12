package com.sgdea.multitenancy.multitenancy.company.application.service;

import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyCreateDto;
import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.company.application.dto.CompanyUpdateDto;
import com.sgdea.multitenancy.multitenancy.company.application.mapper.CompanyMapper;
import com.sgdea.multitenancy.multitenancy.company.application.usecase.CompanyUseCase;
import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import com.sgdea.multitenancy.multitenancy.company.domain.repository.CompanyRepository;
import com.sgdea.multitenancy.multitenancy.companyType.domain.model.CompanyType;
import com.sgdea.multitenancy.multitenancy.companyType.domain.repository.CompanyTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CompanyService implements CompanyUseCase {
    private final CompanyRepository repository;
    private final CompanyTypeRepository companyTypeRepository;
    private final CompanyMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDto findById(UUID id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDto findByCode(String code) {
        Company company = repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new EntityNotFoundException("No existe una empresa con codigo " + code));
        return mapper.toResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy))).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CompanyResponseDto create(CompanyCreateDto dto) {
        if (repository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Ya existe una empresa con codigo " + dto.getCode());
        }
        CompanyType companyType = getCompanyType(dto.getCompanyTypeId());
        return mapper.toResponse(repository.save(mapper.toEntity(dto, companyType)));
    }

    @Override
    @Transactional
    public CompanyResponseDto update(UUID id, CompanyUpdateDto dto) {
        Company company = getEntityById(id);
        if (dto.getCode() != null && repository.existsByCodeIgnoreCaseAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Ya existe una empresa con codigo " + dto.getCode());
        }
        CompanyType companyType = getCompanyType(dto.getCompanyTypeId());
        mapper.updateEntity(company, dto, companyType);
        return mapper.toResponse(repository.save(company));
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
        Company company = getEntityById(id);
        company.setActive(!Boolean.TRUE.equals(company.getActive()));
        repository.save(company);
        return company.getActive() ? "Empresa activada correctamente" : "Empresa desactivada correctamente";
    }

    private Company getEntityById(UUID id) {
        return repository.getReferenceById(id);
    }

    private CompanyType getCompanyType(Long companyTypeId) {
        if (companyTypeId == null) {
            return null;
        }
        return companyTypeRepository.findById(companyTypeId)
                .orElseThrow(() -> new EntityNotFoundException("No existe un tipo de empresa con id " + companyTypeId));
    }
}
