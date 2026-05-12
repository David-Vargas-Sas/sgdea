package com.sgdea.multitenancy.multitenancy.entrypoints.auth.rest;

import com.sgdea.multitenancy.multitenancy.application.auth.dto.AuthLoginRequestDto;
import com.sgdea.multitenancy.multitenancy.application.auth.dto.AuthLoginResponseDto;
import com.sgdea.multitenancy.multitenancy.application.auth.dto.AuthLogoutRequestDto;
import com.sgdea.multitenancy.multitenancy.application.auth.dto.AuthRefreshRequestDto;
import com.sgdea.multitenancy.multitenancy.application.auth.usecase.AuthUseCase;
import com.sgdea.multitenancy.multitenancy.application.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/multitenancy/auth")
@Tag(name = "Autenticacion", description = "Login y logout centralizado de multitenancy")
public class AuthController {
    private final AuthUseCase useCase;

    public AuthController(AuthUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion")
    public ResponseEntity<ApiResponseDto<AuthLoginResponseDto>> login(@Valid @RequestBody AuthLoginRequestDto dto) {
        return ResponseEntity.ok(ApiResponseDto.success("Inicio de sesion correcto", useCase.login(dto)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesion")
    public ResponseEntity<ApiResponseDto<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) AuthLogoutRequestDto dto) {
        String token = extractBearerToken(authorization);
        if (token != null) {
            useCase.logout(token);
        } else {
            useCase.logout(dto);
        }
        return ResponseEntity.ok(ApiResponseDto.success("Sesion cerrada correctamente", null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acceso")
    public ResponseEntity<ApiResponseDto<AuthLoginResponseDto>> refresh(@Valid @RequestBody AuthRefreshRequestDto dto) {
        return ResponseEntity.ok(ApiResponseDto.success("Token renovado correctamente", useCase.refresh(dto)));
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}
