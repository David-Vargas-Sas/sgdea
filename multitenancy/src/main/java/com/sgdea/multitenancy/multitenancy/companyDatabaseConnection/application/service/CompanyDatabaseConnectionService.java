package com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.service;

import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionCreateDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto.CompanyDatabaseConnectionUpdateDto;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.mapper.CompanyDatabaseConnectionMapper;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.usecase.CompanyDatabaseConnectionUseCase;
import com.sgdea.multitenancy.multitenancy.company.domain.model.Company;
import com.sgdea.multitenancy.multitenancy.company.domain.repository.CompanyRepository;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.model.CompanyDatabaseConnection;
import com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.domain.repository.CompanyDatabaseConnectionRepository;
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
public class CompanyDatabaseConnectionService implements CompanyDatabaseConnectionUseCase {
    private final CompanyDatabaseConnectionRepository repository;
    private final CompanyRepository companyRepository;
    private final CompanyDatabaseConnectionMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public List<CompanyDatabaseConnectionResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDatabaseConnectionResponseDto findById(UUID id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDatabaseConnectionResponseDto> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyDatabaseConnectionResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy))).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CompanyDatabaseConnectionResponseDto create(CompanyDatabaseConnectionCreateDto dto) {
        Company company = getCompany(dto.getCompanyId());
        validateUniqueConnectionName(dto.getCompanyId(), dto.getConnectionName(), null);
        CompanyDatabaseConnection connection = repository.save(mapper.toEntity(dto, company));
        return mapper.toResponse(connection);
    }

    @Override
    @Transactional
    public CompanyDatabaseConnectionResponseDto update(UUID id, CompanyDatabaseConnectionUpdateDto dto) {
        CompanyDatabaseConnection connection = getEntityById(id);
        Company company = dto.getCompanyId() == null ? null : getCompany(dto.getCompanyId());
        UUID companyId = dto.getCompanyId() == null ? connection.getCompany().getId() : dto.getCompanyId();
        String connectionName = dto.getConnectionName() == null ? connection.getConnectionName() : dto.getConnectionName();
        validateUniqueConnectionName(companyId, connectionName, id);
        mapper.updateEntity(connection, dto, company);
        return mapper.toResponse(repository.save(connection));
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
        CompanyDatabaseConnection connection = getEntityById(id);
        connection.setActive(!Boolean.TRUE.equals(connection.getActive()));
        repository.save(connection);
        return connection.getActive()
                ? "Conexion de base de datos activada correctamente"
                : "Conexion de base de datos desactivada correctamente";
    }

    private void validateUniqueConnectionName(UUID companyId, String connectionName, UUID currentId) {
        boolean exists = currentId == null
                ? repository.existsByCompanyIdAndConnectionNameIgnoreCase(companyId, connectionName)
                : repository.existsByCompanyIdAndConnectionNameIgnoreCaseAndIdNot(companyId, connectionName, currentId);
        if (exists) {
            throw new IllegalArgumentException("Ya existe una conexion de base de datos con nombre " + connectionName + " para esta empresa");
        }
    }

    private CompanyDatabaseConnection getEntityById(UUID id) {
        return repository.getReferenceById(id);
    }

    private Company getCompany(UUID id) {
        return companyRepository.getReferenceById(id);
    }
}
