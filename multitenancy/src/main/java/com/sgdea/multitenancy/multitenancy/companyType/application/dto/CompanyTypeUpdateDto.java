package com.sgdea.multitenancy.multitenancy.companyType.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyTypeUpdateDto {
    @Size(max = 25, message = "El nombre no puede superar 25 caracteres")
    private String name;

    @Size(max = 100, message = "El usuario actualizador no puede superar 100 caracteres")
    private String updatedBy;
}
