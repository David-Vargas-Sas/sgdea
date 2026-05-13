package com.sgdea.multitenancy.multitenancy.licenseType.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseTypeUpdateDto {
    @Size(max = 30, message = "El codigo no puede superar 30 caracteres")
    private String code;

    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
    private String description;

    private Boolean active;
}
