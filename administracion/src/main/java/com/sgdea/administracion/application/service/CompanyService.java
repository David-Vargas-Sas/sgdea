package com.sgdea.administracion.application.service;

import com.sgdea.administracion.domain.dto.company.CompanyCreateDto;
import com.sgdea.administracion.domain.dto.company.CompanyResponseDto;
import com.sgdea.administracion.domain.dto.company.CompanyUpdateDto;
import com.sgdea.administracion.domain.entity.company.Company;
import com.sgdea.administracion.domain.mapper.company.CompanyMapper;
import com.sgdea.administracion.infraestructure.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDto> findAll() {
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDto findById(UUID id) {
        return companyMapper.toResponse(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public CompanyResponseDto findByIdBasic(UUID id) {
        return findById(id);
    }

    @Transactional(readOnly = true)
    public CompanyResponseDto findByCode(String code) {
        Company company = companyRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new EntityNotFoundException("No existe una compania con codigo " + code));
        return companyMapper.toResponse(company);
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return companyRepository.findAll(pageable).map(companyMapper::toResponse);
    }

    @Transactional
    public CompanyResponseDto create(CompanyCreateDto dto) {
        if (companyRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Ya existe una compania con codigo " + dto.getCode());
        }

        Company company = companyMapper.toEntity(dto);
        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponseDto update(UUID id, CompanyUpdateDto dto) {
        Company company = getEntityById(id);

        if (dto.getCode() != null && companyRepository.existsByCodeIgnoreCaseAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Ya existe una compania con codigo " + dto.getCode());
        }

        companyMapper.updateEntity(company, dto);
        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Transactional
    public Boolean delete(UUID id) {
        Company company = getEntityById(id);
        companyRepository.delete(company);
        return true;
    }

    @Transactional
    public String toggleActive(UUID id) {
        Company company = getEntityById(id);
        Boolean currentStatus = Boolean.TRUE.equals(company.getActive());
        company.setActive(!currentStatus);
        companyRepository.save(company);
        return company.getActive() ? "Empresa activada correctamente" : "Empresa desactivada correctamente";
    }

    @Transactional
    public void uploadLogo(UUID id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo del logo es obligatorio");
        }

        Company company = getEntityById(id);
        String originalFilename = file.getOriginalFilename() == null ? "logo" : file.getOriginalFilename();
        company.setLogoPath("companies/" + id + "/logo/" + originalFilename);
        companyRepository.save(company);
    }

    private Company getEntityById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe una compania con id " + id));
    }
}
