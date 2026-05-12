package com.sgdea.multitenancy.multitenancy.role.application.service;

import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleCreateDto;
import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleResponseDto;
import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleUpdateDto;
import com.sgdea.multitenancy.multitenancy.role.application.mapper.RoleMapper;
import com.sgdea.multitenancy.multitenancy.role.application.usecase.RoleUseCase;
import com.sgdea.multitenancy.multitenancy.role.domain.model.Role;
import com.sgdea.multitenancy.multitenancy.role.domain.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class RoleService implements RoleUseCase {
    private final RoleRepository repository;
    private final RoleMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto findById(Long id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto findByCode(String code) {
        Role role = repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new EntityNotFoundException("No existe un rol con codigo " + code));
        return mapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy))).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public RoleResponseDto create(RoleCreateDto dto) {
        if (repository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Ya existe un rol con codigo " + dto.getCode());
        }
        if (repository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Ya existe un rol con nombre " + dto.getName());
        }
        return mapper.toResponse(repository.save(mapper.toEntity(dto)));
    }

    @Override
    @Transactional
    public RoleResponseDto update(Long id, RoleUpdateDto dto) {
        Role role = getEntityById(id);
        if (dto.getCode() != null && repository.existsByCodeIgnoreCaseAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Ya existe un rol con codigo " + dto.getCode());
        }
        if (dto.getName() != null && repository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new IllegalArgumentException("Ya existe un rol con nombre " + dto.getName());
        }
        mapper.updateEntity(role, dto);
        return mapper.toResponse(repository.save(role));
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        repository.delete(getEntityById(id));
        return true;
    }

    @Override
    @Transactional
    public String toggleActive(Long id) {
        Role role = getEntityById(id);
        role.setActive(!Boolean.TRUE.equals(role.getActive()));
        repository.save(role);
        return role.getActive() ? "Rol activado correctamente" : "Rol desactivado correctamente";
    }

    private Role getEntityById(Long id) {
        return repository.getReferenceById(id);
    }
}
