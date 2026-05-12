package com.sgdea.multitenancy.multitenancy.entrypoints.companyType.rest;

import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.dto.CompanyTypeUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.companyType.usecase.CompanyTypeUseCase;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/multitenancy/tipos-empresa")
@Tag(name = "Tipo Empresa", description = "Gestion de Tipos de Empresa")
public class CompanyTypeController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyTypeController.class);

    private final CompanyTypeUseCase useCase;

    public CompanyTypeController(CompanyTypeUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/all")
    @Operation(summary = "Traer todos los tipos de empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ApiResponseDto<List<CompanyTypeResponseDto>>> getAll() {
        logger.info("Solicitud para obtener todos los tipos de empresa");
        return ResponseEntity.ok(ApiResponseDto.success("Tipos de empresa obtenidos correctamente", useCase.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Traer tipo de empresa por ID")
    public ResponseEntity<ApiResponseDto<CompanyTypeResponseDto>> getById(
            @PathVariable @Parameter(description = "ID del tipo de empresa") Long id) {

        logger.info("Solicitud para obtener tipo de empresa id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Tipo de empresa obtenido correctamente", useCase.findById(id)));
    }

    @GetMapping("/nombre/{name}")
    @Operation(summary = "Traer tipo de empresa por nombre")
    public ResponseEntity<ApiResponseDto<CompanyTypeResponseDto>> getByName(
            @PathVariable @Parameter(description = "Nombre del tipo de empresa") String name) {

        logger.info("Solicitud para obtener tipo de empresa nombre={}", name);
        return ResponseEntity.ok(ApiResponseDto.success("Tipo de empresa obtenido correctamente", useCase.findByName(name)));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Traer tipos de empresa paginados")
    public ResponseEntity<ApiResponseDto<Page<CompanyTypeResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo de ordenamiento") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {

        logger.info("Solicitud paginada de tipos de empresa - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDto.success("Tipos de empresa paginados obtenidos correctamente",
                useCase.findAllPaginated(page, size, sortBy, sortDirection)));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo de empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<ApiResponseDto<CompanyTypeResponseDto>> create(
            @Valid @RequestBody @Parameter(description = "Datos del tipo de empresa") CompanyTypeCreateDto dto) {

        logger.info("Solicitud para crear tipo de empresa");
        CompanyTypeResponseDto created = useCase.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Tipo de empresa creado correctamente", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de empresa")
    public ResponseEntity<ApiResponseDto<CompanyTypeResponseDto>> update(
            @PathVariable @Parameter(description = "ID del tipo de empresa") Long id,
            @Valid @RequestBody CompanyTypeUpdateDto dto) {

        logger.info("Solicitud para actualizar tipo de empresa id={}", id);
        return ResponseEntity.ok(ApiResponseDto.success("Tipo de empresa actualizado correctamente", useCase.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de empresa")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @Parameter(description = "ID del tipo de empresa") Long id) {

        logger.info("Solicitud para eliminar tipo de empresa id={}", id);
        useCase.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Tipo de empresa eliminado correctamente", null));
    }
}
