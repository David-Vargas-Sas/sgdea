package com.sgdea.multitenancy.multitenancy.application.user.service;

import com.sgdea.multitenancy.multitenancy.application.user.dto.UserCreateDto;
import com.sgdea.multitenancy.multitenancy.application.user.dto.UserResponseDto;
import com.sgdea.multitenancy.multitenancy.application.user.dto.UserUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.user.mapper.UserMapper;
import com.sgdea.multitenancy.multitenancy.application.user.usecase.UserUseCase;
import com.sgdea.multitenancy.multitenancy.domain.role.model.Role;
import com.sgdea.multitenancy.multitenancy.domain.role.repository.RoleRepository;
import com.sgdea.multitenancy.multitenancy.domain.user.model.User;
import com.sgdea.multitenancy.multitenancy.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserUseCase {
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, RoleRepository roleRepository, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        User user = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con correo " + email));
        return mapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy))).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponseDto create(UserCreateDto dto) {
        if (repository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con correo " + dto.getEmail());
        }
        if (repository.existsByDocumentNumber(dto.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un usuario con numero de documento " + dto.getDocumentNumber());
        }
        Role role = getRole(dto.getRoleId());
        return mapper.toResponse(repository.save(mapper.toEntity(dto, passwordEncoder.encode(dto.getPassword()), role)));
    }

    @Override
    @Transactional
    public UserResponseDto update(Long id, UserUpdateDto dto) {
        User user = getEntityById(id);
        if (dto.getEmail() != null && repository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)) {
            throw new IllegalArgumentException("Ya existe un usuario con correo " + dto.getEmail());
        }
        if (dto.getDocumentNumber() != null && repository.existsByDocumentNumberAndIdNot(dto.getDocumentNumber(), id)) {
            throw new IllegalArgumentException("Ya existe un usuario con numero de documento " + dto.getDocumentNumber());
        }
        String passwordHash = dto.getPassword() == null ? null : passwordEncoder.encode(dto.getPassword());
        Role role = dto.getRoleId() == null ? null : getRole(dto.getRoleId());
        mapper.updateEntity(user, dto, passwordHash, role);
        return mapper.toResponse(repository.save(user));
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
        User user = getEntityById(id);
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        repository.save(user);
        return user.getActive() ? "Usuario activado correctamente" : "Usuario desactivado correctamente";
    }

    private User getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con id " + id));
    }

    private Role getRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("No existe un rol con id " + roleId));
    }
}
