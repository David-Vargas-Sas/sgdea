package com.sgdea.multitenancy.multitenancy.entrypoints.company.rest;

import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyCreateDto;
import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyResponseDto;
import com.sgdea.multitenancy.multitenancy.application.company.dto.CompanyUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.company.usecase.CompanyUseCase;
import com.sgdea.multitenancy.multitenancy.application.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/multitenancy/empresas")
@Tag(name = "Empresas", description = "Gestion de empresas")
public class CompanyController {
    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);
    private final CompanyUseCase useCase;

    public CompanyController(CompanyUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todas las empresas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ApiResponseDto<List<CompanyResponseDto>>> getAll() {
        logger.info("Solicitud para obtener todas las empresas");
        return ResponseEntity.ok(ApiResponseDto.success("Empresas obtenidas correctamente", useCase.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empresa por ID")
    public ResponseEntity<ApiResponseDto<CompanyResponseDto>> getById(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id) {
        logger.info("Solicitud para obtener empresa id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Empresa obtenida correctamente", useCase.findById(id)));
    }

    @GetMapping("/codigo/{code}")
    @Operation(summary = "Obtener empresa por codigo")
    public ResponseEntity<ApiResponseDto<CompanyResponseDto>> getByCode(
            @PathVariable @Parameter(description = "Codigo de la empresa") String code) {
        logger.info("Solicitud para obtener empresa codigo={}", code);
        return ResponseEntity.ok(ApiResponseDto.success("Empresa obtenida correctamente", useCase.findByCode(code)));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener empresas paginadas")
    public ResponseEntity<ApiResponseDto<Page<CompanyResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo de ordenamiento") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success("Empresas paginadas obtenidas correctamente",
                useCase.findAllPaginated(page, size, sortBy, sortDirection)));
    }

    @PostMapping
    @Operation(summary = "Crear empresa")
    public ResponseEntity<ApiResponseDto<CompanyResponseDto>> create(@Valid @RequestBody CompanyCreateDto dto) {
        logger.info("Solicitud para crear empresa");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Empresa creada correctamente", useCase.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empresa")
    public ResponseEntity<ApiResponseDto<CompanyResponseDto>> update(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id,
            @Valid @RequestBody CompanyUpdateDto dto) {
        logger.info("Solicitud para actualizar empresa id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Empresa actualizada correctamente", useCase.update(id, dto)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar empresa")
    public ResponseEntity<ApiResponseDto<Void>> toggleStatus(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id) {
        logger.info("Solicitud para cambiar estado de empresa id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success(useCase.toggleActive(id), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empresa")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id) {
        logger.info("Solicitud para eliminar empresa id={}", id);
        useCase.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Empresa eliminada correctamente", null));
    }
}
