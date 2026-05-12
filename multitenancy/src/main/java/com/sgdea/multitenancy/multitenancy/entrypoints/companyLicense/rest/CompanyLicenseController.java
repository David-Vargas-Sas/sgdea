package com.sgdea.multitenancy.multitenancy.entrypoints.companyLicense.rest;

import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.dto.CompanyLicenseUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.companyLicense.usecase.CompanyLicenseUseCase;
import com.sgdea.multitenancy.multitenancy.application.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/multitenancy/empresas-licencias")
@Tag(name = "Empresas licencias", description = "Gestion de licencias asignadas a empresas")
public class CompanyLicenseController {
    private static final Logger logger = LoggerFactory.getLogger(CompanyLicenseController.class);
    private final CompanyLicenseUseCase useCase;

    public CompanyLicenseController(CompanyLicenseUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todas las licencias de empresas")
    public ResponseEntity<ApiResponseDto<List<CompanyLicenseResponseDto>>> getAll() {
        logger.info("Solicitud para obtener todas las licencias de empresas");
        return ResponseEntity.ok(ApiResponseDto.success("Licencias de empresas obtenidas correctamente", useCase.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener licencia de empresa por ID")
    public ResponseEntity<ApiResponseDto<CompanyLicenseResponseDto>> getById(
            @PathVariable @Parameter(description = "ID de la licencia de empresa") UUID id) {
        return ResponseEntity.ok(ApiResponseDto.success("Licencia de empresa obtenida correctamente", useCase.findById(id)));
    }

    @GetMapping("/empresa/{companyId}")
    @Operation(summary = "Obtener licencias por empresa")
    public ResponseEntity<ApiResponseDto<List<CompanyLicenseResponseDto>>> getByCompanyId(
            @PathVariable @Parameter(description = "ID de la empresa") UUID companyId) {
        return ResponseEntity.ok(ApiResponseDto.success("Licencias de la empresa obtenidas correctamente",
                useCase.findByCompanyId(companyId)));
    }

    @GetMapping("/tipo-licencia/{licenseTypeId}")
    @Operation(summary = "Obtener licencias por tipo de licencia")
    public ResponseEntity<ApiResponseDto<List<CompanyLicenseResponseDto>>> getByLicenseTypeId(
            @PathVariable @Parameter(description = "ID del tipo de licencia") UUID licenseTypeId) {
        return ResponseEntity.ok(ApiResponseDto.success("Licencias por tipo de licencia obtenidas correctamente",
                useCase.findByLicenseTypeId(licenseTypeId)));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener licencias de empresas paginadas")
    public ResponseEntity<ApiResponseDto<Page<CompanyLicenseResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success("Licencias de empresas paginadas obtenidas correctamente",
                useCase.findAllPaginated(page, size, sortBy, sortDirection)));
    }

    @PostMapping
    @Operation(summary = "Crear licencia de empresa")
    public ResponseEntity<ApiResponseDto<CompanyLicenseResponseDto>> create(@Valid @RequestBody CompanyLicenseCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Licencia de empresa creada correctamente", useCase.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar licencia de empresa")
    public ResponseEntity<ApiResponseDto<CompanyLicenseResponseDto>> update(
            @PathVariable @Parameter(description = "ID de la licencia de empresa") UUID id,
            @Valid @RequestBody CompanyLicenseUpdateDto dto) {
        return ResponseEntity.ok(ApiResponseDto.success("Licencia de empresa actualizada correctamente", useCase.update(id, dto)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar licencia de empresa")
    public ResponseEntity<ApiResponseDto<Void>> toggleStatus(
            @PathVariable @Parameter(description = "ID de la licencia de empresa") UUID id) {
        return ResponseEntity.ok(ApiResponseDto.success(useCase.toggleActive(id), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar licencia de empresa")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID de la licencia de empresa") UUID id) {
        useCase.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Licencia de empresa eliminada correctamente", null));
    }
}
