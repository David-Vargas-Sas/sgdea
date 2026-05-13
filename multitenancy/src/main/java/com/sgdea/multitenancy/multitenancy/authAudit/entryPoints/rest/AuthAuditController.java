package com.sgdea.multitenancy.multitenancy.authAudit.entryPoints.rest;

import com.sgdea.multitenancy.multitenancy.authAudit.application.dto.AuthAuditResponseDto;
import com.sgdea.multitenancy.multitenancy.authAudit.application.usecase.AuthAuditUseCase;
import com.sgdea.multitenancy.multitenancy.securityConfig.application.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/multitenancy/auditoria-accesos")
@Tag(name = "Auditoria de accesos", description = "Consulta de eventos de autenticacion")
public class AuthAuditController {
    private final AuthAuditUseCase useCase;

    public AuthAuditController(AuthAuditUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/paginado")
    @Operation(summary = "Obtener auditoria de accesos paginada")
    public ResponseEntity<ApiResponseDto<Page<AuthAuditResponseDto>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseDto.success("Auditoria de accesos obtenida correctamente",
                useCase.findAllPaginated(page, size)));
    }
}
