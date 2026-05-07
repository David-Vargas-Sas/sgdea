package com.sgdea.administracion.multitenancy.application.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CompanyCreateDto {
    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 30, message = "El codigo no puede superar 30 caracteres")
    private String code;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String name;

    @Size(max = 20, message = "El NIT no puede superar 20 caracteres")
    @Pattern(regexp = "^[0-9.-]*$", message = "El NIT solo puede contener numeros, puntos o guiones")
    private String nit;

    private Integer dv;

    @Size(max = 500, message = "La ruta del logo no puede superar 500 caracteres")
    private String logoPath;

    private String createdBy;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public Integer getDv() {
        return dv;
    }

    public void setDv(Integer dv) {
        this.dv = dv;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
