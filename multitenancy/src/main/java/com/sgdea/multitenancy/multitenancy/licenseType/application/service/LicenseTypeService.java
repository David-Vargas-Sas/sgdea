package com.sgdea.multitenancy.multitenancy.licenseType.application.service;

import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeUpdateDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.mapper.LicenseTypeMapper;
import com.sgdea.multitenancy.multitenancy.licenseType.application.usecase.LicenseTypeUseCase;
import com.sgdea.multitenancy.multitenancy.licenseType.domain.model.LicenseType;
import com.sgdea.multitenancy.multitenancy.licenseType.domain.repository.LicenseTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LicenseTypeService implements LicenseTypeUseCase {
    private final LicenseTypeRepository repository;
    private final LicenseTypeMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public List<LicenseTypeResponseDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LicenseTypeResponseDto findById(UUID id) {
        return mapper.toResponseDTO(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public LicenseTypeResponseDto findByCode(String code) {
        LicenseType licenseType = repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new EntityNotFoundException("No existe un tipo de licencia con codigo " + code));
        return mapper.toResponseDTO(licenseType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LicenseTypeResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return repository.findAll(pageable).map(mapper::toResponseDTO);
    }

    @Override
    @Transactional
    public LicenseTypeResponseDto create(LicenseTypeCreateDto dto) {
        if (repository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Ya existe un tipo de licencia con codigo " + dto.getCode());
        }

        LicenseType licenseType = mapper.toEntity(dto);
        return mapper.toResponseDTO(repository.save(licenseType));
    }

    @Override
    @Transactional
    public LicenseTypeResponseDto update(UUID id, LicenseTypeUpdateDto dto) {
        LicenseType licenseType = getEntityById(id);

        if (dto.getCode() != null && repository.existsByCodeIgnoreCaseAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Ya existe un tipo de licencia con codigo " + dto.getCode());
        }

        mapper.updateEntityFromDTO(dto, licenseType);
        return mapper.toResponseDTO(repository.save(licenseType));
    }

    @Override
    @Transactional
    public Boolean delete(UUID id) {
        LicenseType licenseType = getEntityById(id);
        repository.delete(licenseType);
        return true;
    }

    @Override
    @Transactional
    public String toggleActive(UUID id) {
        LicenseType licenseType = getEntityById(id);
        Boolean currentStatus = Boolean.TRUE.equals(licenseType.getActive());
        licenseType.setActive(!currentStatus);
        repository.save(licenseType);
        return licenseType.getActive()
                ? "Tipo de licencia activado correctamente"
                : "Tipo de licencia desactivado correctamente";
    }

    private LicenseType getEntityById(UUID id) {
        return repository.getReferenceById(id);
    }
}
