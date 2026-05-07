package com.sgdea.administracion.multitenancy.entrypoints.rest.user;

import com.sgdea.administracion.multitenancy.application.dto.user.UserCreateDto;
import com.sgdea.administracion.multitenancy.application.dto.user.UserResponseDto;
import com.sgdea.administracion.multitenancy.application.dto.user.UserUpdateDto;
import com.sgdea.administracion.multitenancy.application.services.UserService;
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
@RequestMapping("/administrador/users")
@Tag(name = "Usuarios", description = "Gestion de usuarios")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<UserResponseDto>> getAll() {
        logger.info("Solicitud para obtener todos los usuarios");
        List<UserResponseDto> list = service.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UserResponseDto> getById(
            @PathVariable @Parameter(description = "ID del usuario") UUID id) {

        logger.info("Solicitud para obtener usuario con id: {}", id);
        UserResponseDto dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Obtener usuario por username")
    public ResponseEntity<UserResponseDto> getByUsername(
            @PathVariable @Parameter(description = "Username del usuario") String username) {

        logger.info("Solicitud para obtener usuario con username: {}", username);
        UserResponseDto dto = service.findByUsername(username);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener usuario por correo")
    public ResponseEntity<UserResponseDto> getByEmail(
            @PathVariable @Parameter(description = "Correo del usuario") String email) {

        logger.info("Solicitud para obtener usuario con correo: {}", email);
        UserResponseDto dto = service.findByEmail(email);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Obtener usuarios paginados")
    public ResponseEntity<Page<UserResponseDto>> getAllPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Numero de pagina") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamano de pagina") int size,
            @RequestParam(defaultValue = "id") @Parameter(description = "Campo por el que ordenar") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Direccion de ordenamiento") String sortDirection) {

        logger.info("Solicitud paginada usuarios - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);

        Page<UserResponseDto> result = service.findAllPaginated(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Crear un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<UserResponseDto> create(
            @Valid @RequestBody @Parameter(description = "Datos del usuario") UserCreateDto dto) {

        logger.info("Solicitud para crear usuario");
        UserResponseDto created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable @Parameter(description = "ID del usuario") UUID id,
            @Valid @RequestBody UserUpdateDto dto) {

        logger.info("Solicitud para actualizar usuario id={}", id);
        UserResponseDto updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activar o desactivar usuario")
    public ResponseEntity<String> toggleStatus(
            @PathVariable @Parameter(description = "ID del usuario") UUID id) {

        logger.info("Solicitud para cambiar estado de usuario id={}", id);
        String message = service.toggleActive(id);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> delete(
            @PathVariable @Parameter(description = "ID del usuario") UUID id) {

        logger.info("Solicitud para eliminar usuario id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
