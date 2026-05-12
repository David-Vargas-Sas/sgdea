package com.sgdea.multitenancy.multitenancy.user.entryPoints.rest;

import com.sgdea.multitenancy.multitenancy.securityConfig.application.dto.ApiResponseDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserCreateDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserResponseDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserUpdateDto;
import com.sgdea.multitenancy.multitenancy.user.application.usecase.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/multitenancy/usuarios")
@Tag(name = "Usuarios", description = "Gestion de usuarios")
public class UserController {
    private final UserUseCase useCase;

    public UserController(UserUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los usuarios")
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponseDto.success("Usuarios obtenidos correctamente", useCase.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getById(@PathVariable @Parameter(description = "ID del usuario") Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("Usuario obtenido correctamente", useCase.findById(id)));
    }

    @GetMapping("/correo/{email}")
    @Operation(summary = "Obtener usuario por correo")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponseDto.success("Usuario obtenido correctamente", useCase.findByEmail(email)));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener usuarios paginados")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(ApiResponseDto.success("Usuarios paginados obtenidos correctamente",
                useCase.findAllPaginated(page, size, sortBy, sortDirection)));
    }

    @PostMapping
    @Operation(summary = "Crear usuario")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> create(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Usuario creado correctamente", useCase.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(ApiResponseDto.success("Usuario actualizado correctamente", useCase.update(id, dto)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar usuario")
    public ResponseEntity<ApiResponseDto<Void>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success(useCase.toggleActive(id), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Usuario eliminado correctamente", null));
    }
}
