package com.sgdea.multitenancy.multitenancy.entrypoints.companyProvisioning.rest;

import com.sgdea.multitenancy.multitenancy.application.common.dto.ApiResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto.CompanyProvisioningCreateDto;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.dto.CompanyProvisioningResponseDto;
import com.sgdea.multitenancy.multitenancy.application.companyProvisioning.usecase.CompanyProvisioningUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/multitenancy/provisionamiento-empresas")
@Tag(name = "Provisionamiento de empresas", description = "Flujo completo para crear empresa, usuario administrador y conexion")
public class CompanyProvisioningController {
    private final CompanyProvisioningUseCase useCase;

    public CompanyProvisioningController(CompanyProvisioningUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Crear empresa con usuario administrador y conexion")
    public ResponseEntity<ApiResponseDto<CompanyProvisioningResponseDto>> create(@Valid @RequestBody CompanyProvisioningCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Empresa provisionada correctamente", useCase.create(dto)));
    }
}
