package com.sgdea.administracion.multitenancy.entrypoints.rest.company;

import com.sgdea.administracion.multitenancy.application.dto.company.CompanyCreateDto;
import com.sgdea.administracion.multitenancy.application.dto.company.CompanyResponseDto;
import com.sgdea.administracion.multitenancy.application.dto.company.CompanyUpdateDto;
import com.sgdea.administracion.multitenancy.application.services.CompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/administrador/companies")
@Tag(name = "Empresas", description = "Gestion de empresas")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    // =========================
    // OBTENER TODOS
    // =========================
    @GetMapping("/all")
    @Operation(summary = "Obtener todas las empresas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<CompanyResponseDto>> getAll() {
        logger.info("Solicitud para obtener todas las empresas");
        List<CompanyResponseDto> list = service.findAll();
        return ResponseEntity.ok(list);
    }

    // =========================
    // OBTENER POR ID
    // =========================
    @GetMapping("/{id}")
    @Operation(summary = "Obtener empresa por ID")
    public ResponseEntity<CompanyResponseDto> getById(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id) {

        logger.info("Solicitud para obtener empresa con id: {}", id);
        CompanyResponseDto dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/basic")
    @Operation(summary = "Obtener datos basicos de empresa por ID")
    public ResponseEntity<CompanyResponseDto> getByIdBasic(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id) {

        logger.info("Solicitud para obtener datos basicos de empresa con id: {}", id);
        CompanyResponseDto dto = service.findByIdBasic(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Obtener empresa por codigo")
    public ResponseEntity<CompanyResponseDto> getByCode(
            @PathVariable @Parameter(description = "Codigo de la empresa") String code) {

        logger.info("Solicitud para obtener empresa con codigo: {}", code);
        CompanyResponseDto dto = service.findByCode(code);
        return ResponseEntity.ok(dto);
    }

    // =========================
    // OBTENER PAGINADO
    // =========================
    @GetMapping("/paginated")
    @Operation(summary = "Obtener empresas paginadas")
    public ResponseEntity<Page<CompanyResponseDto>> getAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {

        logger.info("Solicitud paginada - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);

        Page<CompanyResponseDto> result = service.findAllPaginated(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(result);
    }

    // =========================
    // CREAR
    // =========================
    @PostMapping
    @Operation(summary = "Crear una empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<CompanyResponseDto> create(
            @Valid
            @RequestBody
            @Parameter(description = "Datos de la empresa") CompanyCreateDto dto) {

        logger.info("Solicitud para crear empresa");
        CompanyResponseDto created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empresa")
    public ResponseEntity<CompanyResponseDto> update(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id,
            @Valid @RequestBody CompanyUpdateDto dto) {

        logger.info("Solicitud para actualizar empresa id={}", id);
        CompanyResponseDto updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // =========================
    // ACTIVAR / DESACTIVAR
    // =========================
    @PatchMapping("/{id}/status")
    @Operation(summary = "Activar o desactivar empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public ResponseEntity<String> toggleStatus(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id) {

        logger.info("Solicitud para cambiar estado de empresa id={}", id);
        String message = service.toggleActive(id);
        return ResponseEntity.ok(message);
    }

    // =========================
    // SUBIR / ACTUALIZAR LOGO
    // =========================
    @PostMapping(value = "/{id}/logo", consumes = "multipart/form-data")
    @Operation(summary = "Subir o actualizar logo de la empresa")
    public ResponseEntity<String> uploadLogo(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id,
            @RequestParam("file") MultipartFile file) {

        logger.info("Solicitud para subir logo empresa id={}", id);
        service.uploadLogo(id, file);
        return ResponseEntity.ok("Logo actualizado correctamente");
    }

    // =========================
    // ELIMINAR
    // =========================
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empresa")
    public ResponseEntity<Void> delete(
            @PathVariable @Parameter(description = "ID de la empresa") UUID id) {

        logger.info("Solicitud para eliminar empresa id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
