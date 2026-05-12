package com.sgdea.multitenancy.multitenancy.entrypoints.companyDatabaseConnection.rest;

import com.sgdea.multitenancy.multitenancy.application.common.dto.ApiResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.dto.CompanyDatabaseConnectionUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.companyDatabaseConnection.usecase.CompanyDatabaseConnectionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/multitenancy/empresas-conexiones-bd")
@Tag(name = "Empresas conexiones BD", description = "Gestion de conexiones de base de datos por empresa")
public class CompanyDatabaseConnectionController {
    private static final Logger logger = LoggerFactory.getLogger(CompanyDatabaseConnectionController.class);
    private final CompanyDatabaseConnectionUseCase useCase;

    public CompanyDatabaseConnectionController(CompanyDatabaseConnectionUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todas las conexiones de base de datos")
    public ResponseEntity<ApiResponseDto<List<CompanyDatabaseConnectionResponseDto>>> getAll() {
        logger.info("Solicitud para obtener todas las conexiones de base de datos");
        return ResponseEntity.ok(ApiResponseDto.success("Conexiones de base de datos obtenidas correctamente", useCase.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener conexion de base de datos por ID")
    public ResponseEntity<ApiResponseDto<CompanyDatabaseConnectionResponseDto>> getById(
            @PathVariable @Parameter(description = "ID de la conexion de base de datos") UUID id) {
        return ResponseEntity.ok(ApiResponseDto.success("Conexion de base de datos obtenida correctamente", useCase.findById(id)));
    }

    @GetMapping("/empresa/{companyId}")
    @Operation(summary = "Obtener conexiones de base de datos por empresa")
    public ResponseEntity<ApiResponseDto<List<CompanyDatabaseConnectionResponseDto>>> getByCompanyId(
            @PathVariable @Parameter(description = "ID de la empresa") UUID companyId) {
        return ResponseEntity.ok(ApiResponseDto.success("Conexiones de base de datos de la empresa obtenidas correctamente",
                useCase.findByCompanyId(companyId)));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener conexiones de base de datos paginadas")
    public ResponseEntity<ApiResponseDto<Page<CompanyDatabaseConnectionResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo de ordenamiento") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success("Conexiones de base de datos paginadas obtenidas correctamente",
                useCase.findAllPaginated(page, size, sortBy, sortDirection)));
    }

    @PostMapping
    @Operation(summary = "Crear conexion de base de datos")
    public ResponseEntity<ApiResponseDto<CompanyDatabaseConnectionResponseDto>> create(
            @Valid @RequestBody CompanyDatabaseConnectionCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Conexion de base de datos creada correctamente", useCase.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar conexion de base de datos")
    public ResponseEntity<ApiResponseDto<CompanyDatabaseConnectionResponseDto>> update(
            @PathVariable @Parameter(description = "ID de la conexion de base de datos") UUID id,
            @Valid @RequestBody CompanyDatabaseConnectionUpdateDto dto) {
        return ResponseEntity.ok(ApiResponseDto.success("Conexion de base de datos actualizada correctamente", useCase.update(id, dto)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar conexion de base de datos")
    public ResponseEntity<ApiResponseDto<Void>> toggleStatus(
            @PathVariable @Parameter(description = "ID de la conexion de base de datos") UUID id) {
        return ResponseEntity.ok(ApiResponseDto.success(useCase.toggleActive(id), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar conexion de base de datos")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID de la conexion de base de datos") UUID id) {
        useCase.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Conexion de base de datos eliminada correctamente", null));
    }
}
