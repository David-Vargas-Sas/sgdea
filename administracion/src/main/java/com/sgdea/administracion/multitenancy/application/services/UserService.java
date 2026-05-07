package com.sgdea.administracion.multitenancy.application.services;

import com.sgdea.administracion.multitenancy.application.dto.user.UserCreateDto;
import com.sgdea.administracion.multitenancy.application.dto.user.UserResponseDto;
import com.sgdea.administracion.multitenancy.application.dto.user.UserUpdateDto;
import com.sgdea.administracion.multitenancy.application.mapper.user.UserMapper;
import com.sgdea.administracion.multitenancy.domain.model.company.Company;
import com.sgdea.administracion.multitenancy.domain.model.user.User;
import com.sgdea.administracion.multitenancy.domain.repository.CompanyRepository;
import com.sgdea.administracion.multitenancy.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(UUID id) {
        return userMapper.toResponse(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con username " + username));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con correo " + email));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional
    public UserResponseDto create(UserCreateDto dto) {
        validateUniqueUsername(dto.getUsername());
        validateUniqueEmail(dto.getEmail());

        Company company = getCompany(dto.getCompanyId());
        String passwordHash = passwordEncoder.encode(dto.getPassword());
        User user = userMapper.toEntity(dto, company, passwordHash);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto update(UUID id, UserUpdateDto dto) {
        User user = getEntityById(id);

        if (dto.getUsername() != null && userRepository.existsByUsernameIgnoreCaseAndIdNot(dto.getUsername(), id)) {
            throw new IllegalArgumentException("Ya existe un usuario con username " + dto.getUsername());
        }
        if (dto.getEmail() != null && userRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)) {
            throw new IllegalArgumentException("Ya existe un usuario con correo " + dto.getEmail());
        }

        Company company = dto.getCompanyId() == null ? null : getCompany(dto.getCompanyId());
        String passwordHash = dto.getPassword() == null ? null : passwordEncoder.encode(dto.getPassword());
        userMapper.updateEntity(user, dto, company, passwordHash);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public Boolean delete(UUID id) {
        User user = getEntityById(id);
        userRepository.delete(user);
        return true;
    }

    @Transactional
    public String toggleActive(UUID id) {
        User user = getEntityById(id);
        Boolean currentStatus = Boolean.TRUE.equals(user.getActive());
        user.setActive(!currentStatus);
        userRepository.save(user);
        return user.getActive() ? "Usuario activado correctamente" : "Usuario desactivado correctamente";
    }

    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Ya existe un usuario con username " + username);
        }
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con correo " + email);
        }
    }

    private Company getCompany(UUID companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("No existe una compania con id " + companyId));
    }

    private User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con id " + id));
    }
}
