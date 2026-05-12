package com.sgdea.multitenancy.multitenancy.licenseType.entryPoints.rest;

import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.dto.LicenseTypeUpdateDto;
import com.sgdea.multitenancy.multitenancy.licenseType.application.usecase.LicenseTypeUseCase;
import com.sgdea.multitenancy.multitenancy.securityConfig.application.dto.ApiResponseDto;
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
@RequestMapping("/multitenancy/tipos-licencia")
@Tag(name = "Tipos de licencia", description = "Gestion de tipos de licencia")
public class LicenseTypeController {

    private static final Logger logger = LoggerFactory.getLogger(LicenseTypeController.class);

    private final LicenseTypeUseCase useCase;

    public LicenseTypeController(LicenseTypeUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los tipos de licencia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ApiResponseDto<List<LicenseTypeResponseDto>>> getAll() {
        logger.info("Solicitud para obtener todos los tipos de licencia");
        return ResponseEntity.ok(ApiResponseDto.success("Tipos de licencia obtenidos correctamente", useCase.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de licencia por ID")
    public ResponseEntity<ApiResponseDto<LicenseTypeResponseDto>> getById(
            @PathVariable @Parameter(description = "ID del tipo de licencia") UUID id) {

        logger.info("Solicitud para obtener tipo de licencia con id: {}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Tipo de licencia obtenido correctamente", useCase.findById(id)));
    }

    @GetMapping("/codigo/{code}")
    @Operation(summary = "Obtener tipo de licencia por codigo")
    public ResponseEntity<ApiResponseDto<LicenseTypeResponseDto>> getByCode(
            @PathVariable @Parameter(description = "Codigo del tipo de licencia") String code) {

        logger.info("Solicitud para obtener tipo de licencia con codigo: {}", code);
        return ResponseEntity.ok(ApiResponseDto.success("Tipo de licencia obtenido correctamente", useCase.findByCode(code)));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener tipos de licencia paginados")
    public ResponseEntity<ApiResponseDto<Page<LicenseTypeResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {

        logger.info("Solicitud paginada tipos de licencia - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Tipos de licencia paginados obtenidos correctamente",
                useCase.findAllPaginated(page, size, sortBy, sortDirection)));
    }

    @PostMapping
    @Operation(summary = "Crear un tipo de licencia")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<ApiResponseDto<LicenseTypeResponseDto>> create(
            @Valid @RequestBody @Parameter(description = "Datos del tipo de licencia") LicenseTypeCreateDto dto) {

        logger.info("Solicitud para crear tipo de licencia");
        LicenseTypeResponseDto created = useCase.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Tipo de licencia creado correctamente", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de licencia")
    public ResponseEntity<ApiResponseDto<LicenseTypeResponseDto>> update(
            @PathVariable @Parameter(description = "ID del tipo de licencia") UUID id,
            @Valid @RequestBody LicenseTypeUpdateDto dto) {

        logger.info("Solicitud para actualizar tipo de licencia id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Tipo de licencia actualizado correctamente", useCase.update(id, dto)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar tipo de licencia")
    public ResponseEntity<ApiResponseDto<Void>> toggleStatus(
            @PathVariable @Parameter(description = "ID del tipo de licencia") UUID id) {

        logger.info("Solicitud para cambiar estado de tipo de licencia id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success(useCase.toggleActive(id), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de licencia")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID del tipo de licencia") UUID id) {

        logger.info("Solicitud para eliminar tipo de licencia id={}", id);
        useCase.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Tipo de licencia eliminado correctamente", null));
    }
}
