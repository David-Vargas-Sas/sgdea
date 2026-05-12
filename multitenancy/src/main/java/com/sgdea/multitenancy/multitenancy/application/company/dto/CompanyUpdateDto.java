package com.sgdea.multitenancy.multitenancy.application.company.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUpdateDto {
    private Long companyTypeId;

    @Size(max = 50, message = "El codigo no puede superar 50 caracteres")
    private String code;

    @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
    private String name;

    @Size(max = 10, message = "El NIT no puede superar 10 caracteres")
    private String taxId;

    @Size(max = 1, message = "El digito de verificacion no puede superar 1 caracter")
    private String verificationDigit;

    @Size(max = 255, message = "La ruta del logo no puede superar 255 caracteres")
    private String logoPath;

    private Boolean active;

    @Size(max = 100, message = "El usuario actualizador no puede superar 100 caracteres")
    private String updatedBy;
}
