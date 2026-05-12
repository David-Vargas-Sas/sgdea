package com.sgdea.multitenancy.multitenancy.companyUser.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserUpdateDto {
    private UUID companyId;
    private Long userId;
    private Boolean active;

    @Size(max = 100, message = "El usuario actualizador no puede superar 100 caracteres")
    private String updatedBy;
}
