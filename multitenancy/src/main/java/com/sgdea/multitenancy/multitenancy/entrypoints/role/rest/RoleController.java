package com.sgdea.multitenancy.multitenancy.entrypoints.role.rest;

import com.sgdea.multitenancy.multitenancy.application.common.dto.ApiResponseDto;
import com.sgdea.multitenancy.multitenancy.application.role.dto.RoleCreateDto;
import com.sgdea.multitenancy.multitenancy.application.role.dto.RoleResponseDto;
import com.sgdea.multitenancy.multitenancy.application.role.dto.RoleUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.role.usecase.RoleUseCase;
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

@RestController
@RequestMapping("/multitenancy/roles")
@Tag(name = "Roles", description = "Gestion de roles")
public class RoleController {
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    private final RoleUseCase useCase;

    public RoleController(RoleUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los roles")
    public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> getAll() {
        logger.info("Solicitud para obtener todos los roles");
        return ResponseEntity.ok(ApiResponseDto.success("Roles obtenidos correctamente", useCase.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getById(@PathVariable @Parameter(description = "ID del rol") Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("Rol obtenido correctamente", useCase.findById(id)));
    }

    @GetMapping("/codigo/{code}")
    @Operation(summary = "Obtener rol por codigo")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getByCode(@PathVariable @Parameter(description = "Codigo del rol") String code) {
        return ResponseEntity.ok(ApiResponseDto.success("Rol obtenido correctamente", useCase.findByCode(code)));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener roles paginados")
    public ResponseEntity<ApiResponseDto<Page<RoleResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success("Roles paginados obtenidos correctamente",
                useCase.findAllPaginated(page, size, sortBy, sortDirection)));
    }

    @PostMapping
    @Operation(summary = "Crear rol")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> create(@Valid @RequestBody RoleCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Rol creado correctamente", useCase.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDto dto) {
        return ResponseEntity.ok(ApiResponseDto.success("Rol actualizado correctamente", useCase.update(id, dto)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar rol")
    public ResponseEntity<ApiResponseDto<Void>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success(useCase.toggleActive(id), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Rol eliminado correctamente", null));
    }
}
