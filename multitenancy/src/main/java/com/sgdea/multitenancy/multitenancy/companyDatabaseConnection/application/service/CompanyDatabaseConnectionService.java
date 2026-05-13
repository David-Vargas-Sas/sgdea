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
import com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security.DatabaseCredentialEncryptionService;
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
    private final DatabaseCredentialEncryptionService credentialEncryptionService;


    @Override
    @Transactional(readOnly = true)
    public List<CompanyDatabaseConnectionResponseDto> findAll() {
        return repository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDatabaseConnectionResponseDto findById(UUID id) {
        return toResponseDTO(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDatabaseConnectionResponseDto> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).stream().map(this::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyDatabaseConnectionResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return repository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy))).map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public CompanyDatabaseConnectionResponseDto create(CompanyDatabaseConnectionCreateDto dto) {
        Company company = getCompany(dto.getCompanyId());
        validateUniqueConnectionName(dto.getCompanyId(), dto.getConnectionName(), null);
        CompanyDatabaseConnection connection = mapper.toEntity(dto);
        connection.setCompany(company);
        encryptCredentials(connection);
        return toResponseDTO(repository.save(connection));
    }

    @Override
    @Transactional
    public CompanyDatabaseConnectionResponseDto update(UUID id, CompanyDatabaseConnectionUpdateDto dto) {
        CompanyDatabaseConnection connection = getEntityById(id);
        Company company = dto.getCompanyId() == null ? null : getCompany(dto.getCompanyId());
        UUID companyId = dto.getCompanyId() == null ? connection.getCompany().getId() : dto.getCompanyId();
        String connectionName = dto.getConnectionName() == null ? connection.getConnectionName() : dto.getConnectionName();
        validateUniqueConnectionName(companyId, connectionName, id);
        mapper.updateEntityFromDTO(dto, connection);
        if (company != null) {
            connection.setCompany(company);
        }
        encryptCredentials(connection);
        return toResponseDTO(repository.save(connection));
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

    @Transactional
    public int encryptStoredPlaintextCredentials() {
        List<CompanyDatabaseConnection> connections = repository.findAll();
        List<CompanyDatabaseConnection> updatedConnections = new java.util.ArrayList<>();

        for (CompanyDatabaseConnection connection : connections) {
            boolean changed = encryptCredentials(connection);
            if (changed) {
                updatedConnections.add(connection);
            }
        }

        if (!updatedConnections.isEmpty()) {
            repository.saveAll(updatedConnections);
        }

        return updatedConnections.size();
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

    private boolean encryptCredentials(CompanyDatabaseConnection connection) {
        String currentPassword = connection.getEncryptedPassword();
        String currentConnectionString = connection.getEncryptedConnectionString();
        String encryptedPassword = credentialEncryptionService.encryptIfNeeded(currentPassword);
        String encryptedConnectionString = credentialEncryptionService.encryptIfNeeded(currentConnectionString);

        connection.setEncryptedPassword(encryptedPassword);
        connection.setEncryptedConnectionString(encryptedConnectionString);

        return !java.util.Objects.equals(currentPassword, encryptedPassword)
                || !java.util.Objects.equals(currentConnectionString, encryptedConnectionString);
    }

    private CompanyDatabaseConnectionResponseDto toResponseDTO(CompanyDatabaseConnection connection) {
        CompanyDatabaseConnectionResponseDto response = mapper.toResponseDTO(connection);
        response.setEncryptedPassword(null);
        response.setEncryptedConnectionString(null);
        return response;
    }
}
