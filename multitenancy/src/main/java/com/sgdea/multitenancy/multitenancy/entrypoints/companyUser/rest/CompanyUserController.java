package com.sgdea.multitenancy.multitenancy.entrypoints.companyUser.rest;

import com.sgdea.multitenancy.multitenancy.application.common.dto.ApiResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.dto.CompanyUserUpdateDto;
import com.sgdea.multitenancy.multitenancy.application.companyUser.usecase.CompanyUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/multitenancy/empresas-usuarios")
@Tag(name = "Empresas usuarios", description = "Gestion de usuarios por empresa")
public class CompanyUserController {
    private final CompanyUserUseCase useCase;

    public CompanyUserController(CompanyUserUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todas las relaciones empresa usuario")
    public ResponseEntity<ApiResponseDto<List<CompanyUserResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponseDto.success("Usuarios por empresa obtenidos correctamente", useCase.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener relacion empresa usuario por ID")
    public ResponseEntity<ApiResponseDto<CompanyUserResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("Usuario de empresa obtenido correctamente", useCase.findById(id)));
    }

    @GetMapping("/empresa/{companyId}")
    @Operation(summary = "Obtener usuarios por empresa")
    public ResponseEntity<ApiResponseDto<List<CompanyUserResponseDto>>> getByCompanyId(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponseDto.success("Usuarios de la empresa obtenidos correctamente", useCase.findByCompanyId(companyId)));
    }

    @GetMapping("/usuario/{userId}")
    @Operation(summary = "Obtener empresas por usuario")
    public ResponseEntity<ApiResponseDto<List<CompanyUserResponseDto>>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseDto.success("Empresas del usuario obtenidas correctamente", useCase.findByUserId(userId)));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener usuarios por empresa paginados")
    public ResponseEntity<ApiResponseDto<Page<CompanyUserResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success("Usuarios por empresa paginados obtenidos correctamente",
                useCase.findAllPaginated(page, size, sortBy, sortDirection)));
    }

    @PostMapping
    @Operation(summary = "Asociar usuario a empresa")
    public ResponseEntity<ApiResponseDto<CompanyUserResponseDto>> create(@Valid @RequestBody CompanyUserCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Usuario asociado a la empresa correctamente", useCase.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario de empresa")
    public ResponseEntity<ApiResponseDto<CompanyUserResponseDto>> update(@PathVariable Long id, @Valid @RequestBody CompanyUserUpdateDto dto) {
        return ResponseEntity.ok(ApiResponseDto.success("Usuario de empresa actualizado correctamente", useCase.update(id, dto)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar usuario de empresa")
    public ResponseEntity<ApiResponseDto<Void>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success(useCase.toggleActive(id), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario de empresa")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Usuario de empresa eliminado correctamente", null));
    }
}
