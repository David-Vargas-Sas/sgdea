package com.sgdea.multitenancy.multitenancy.companyUser.application.service;

import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserCreateDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.dto.CompanyUserUpdateDto;
import com.sgdea.multitenancy.multitenancy.companyUser.application.mapper.CompanyUserMapper;
import com.sgdea.multitenancy.multitenancy.companyUser.application.usecase.CompanyUserUseCase;
import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import com.sgdea.multitenancy.multitenancy.company.domain.repository.CompanyRepository;
import com.sgdea.multitenancy.multitenancy.companyUser.domain.model.CompanyUser;
import com.sgdea.multitenancy.multitenancy.companyUser.domain.repository.CompanyUserRepository;
import com.sgdea.multitenancy.multitenancy.user.domain.model.User;
import com.sgdea.multitenancy.multitenancy.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CompanyUserService implements CompanyUserUseCase {
    private final CompanyUserRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyUserMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyUserResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyUserResponseDto findById(Long id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyUserResponseDto> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyUserResponseDto> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyUserResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy))).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CompanyUserResponseDto create(CompanyUserCreateDto dto) {
        if (repository.existsByCompanyIdAndUserId(dto.getCompanyId(), dto.getUserId())) {
            throw new IllegalArgumentException("El usuario ya esta asociado a esta empresa");
        }
        Company company = getCompany(dto.getCompanyId());
        User user = getUser(dto.getUserId());
        return mapper.toResponse(repository.save(mapper.toEntity(dto, company, user)));
    }

    @Override
    @Transactional
    public CompanyUserResponseDto update(Long id, CompanyUserUpdateDto dto) {
        CompanyUser companyUser = getEntityById(id);
        UUID companyId = dto.getCompanyId() == null ? companyUser.getCompany().getId() : dto.getCompanyId();
        Long userId = dto.getUserId() == null ? companyUser.getUser().getId() : dto.getUserId();
        if (repository.existsByCompanyIdAndUserIdAndIdNot(companyId, userId, id)) {
            throw new IllegalArgumentException("El usuario ya esta asociado a esta empresa");
        }
        Company company = dto.getCompanyId() == null ? null : getCompany(dto.getCompanyId());
        User user = dto.getUserId() == null ? null : getUser(dto.getUserId());
        mapper.updateEntity(companyUser, dto, company, user);
        return mapper.toResponse(repository.save(companyUser));
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
        CompanyUser companyUser = getEntityById(id);
        companyUser.setActive(!Boolean.TRUE.equals(companyUser.getActive()));
        repository.save(companyUser);
        return companyUser.getActive() ? "Usuario de empresa activado correctamente" : "Usuario de empresa desactivado correctamente";
    }

    private CompanyUser getEntityById(Long id) {
        return repository.getReferenceById(id);
    }

    private Company getCompany(UUID companyId) {
        return companyRepository.getReferenceById(companyId);
    }

    private User getUser(Long userId) {
        return userRepository.getReferenceById(userId);
    }

}
