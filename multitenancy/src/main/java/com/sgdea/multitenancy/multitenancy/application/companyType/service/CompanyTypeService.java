package com.sgdea.multitenancy.multitenancy.application.companyType.service;

import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.mapper.CompanyTypeMapper;
import com.sgdea.multitenancy.multitenancy.application.companyType.usecase.CompanyTypeUseCase;
import com.sgdea.multitenancy.multitenancy.domain.companyType.model.CompanyType;
import com.sgdea.multitenancy.multitenancy.domain.companyType.repository.CompanyTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyTypeService implements CompanyTypeUseCase {
    private final CompanyTypeRepository repository;
    private final CompanyTypeMapper mapper;

    public CompanyTypeService(CompanyTypeRepository repository, CompanyTypeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyTypeResponseDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyTypeResponseDto findById(Long id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyTypeResponseDto findByName(String name) {
        CompanyType companyType = repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new EntityNotFoundException("No existe un tipo de empresa con nombre " + name));
        return mapper.toResponse(companyType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyTypeResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CompanyTypeResponseDto create(CompanyTypeCreateDto dto) {
        if (repository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Ya existe un tipo de empresa con nombre " + dto.getName());
        }

        CompanyType companyType = mapper.toEntity(dto);
        return mapper.toResponse(repository.save(companyType));
    }

    @Override
    @Transactional
    public CompanyTypeResponseDto update(Long id, CompanyTypeUpdateDto dto) {
        CompanyType companyType = getEntityById(id);

        if (dto.getName() != null && repository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new IllegalArgumentException("Ya existe un tipo de empresa con nombre " + dto.getName());
        }

        mapper.updateEntity(companyType, dto);
        return mapper.toResponse(repository.save(companyType));
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        CompanyType companyType = getEntityById(id);
        repository.delete(companyType);
        return true;
    }

    private CompanyType getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe un tipo de empresa con id " + id));
    }
}
