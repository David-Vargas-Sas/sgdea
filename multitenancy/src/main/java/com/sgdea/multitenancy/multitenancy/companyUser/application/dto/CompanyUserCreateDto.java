package com.sgdea.multitenancy.multitenancy.companyUser.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserCreateDto {
    @NotNull(message = "La empresa es obligatoria")
    private UUID companyId;

    @NotNull(message = "El usuario es obligatorio")
    private Long userId;

    @Size(max = 100, message = "El usuario creador no puede superar 100 caracteres")
    private String createdBy;
}
