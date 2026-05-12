package com.sgdea.multitenancy.multitenancy.companyType.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyTypeCreateDto {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 25, message = "El nombre no puede superar 25 caracteres")
    private String name;

    @Size(max = 100, message = "El usuario creador no puede superar 100 caracteres")
    private String createdBy;
}
