package com.sgdea.multitenancy.multitenancy.companyDatabaseConnection.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDatabaseConnectionCreateDto {
    @NotNull(message = "El id de la empresa es obligatorio")
    private UUID companyId;

    @NotBlank(message = "El nombre de la conexion es obligatorio")
    @Size(max = 100, message = "El nombre de la conexion no puede superar 100 caracteres")
    private String connectionName;

    @NotBlank(message = "El proveedor es obligatorio")
    @Size(max = 50, message = "El proveedor no puede superar 50 caracteres")
    private String provider;

    @NotBlank(message = "El servidor es obligatorio")
    @Size(max = 255, message = "El servidor no puede superar 255 caracteres")
    private String server;

    @NotBlank(message = "El nombre de la base de datos es obligatorio")
    @Size(max = 255, message = "El nombre de la base de datos no puede superar 255 caracteres")
    private String databaseName;

    @Min(value = 1, message = "El puerto debe ser mayor a 0")
    @Max(value = 65535, message = "El puerto no puede superar 65535")
    private Integer port;

    @Size(max = 255, message = "El usuario de base de datos no puede superar 255 caracteres")
    private String databaseUser;

    @Size(max = 1000, message = "La contrasena encriptada no puede superar 1000 caracteres")
    private String encryptedPassword;

    @Size(max = 2000, message = "La cadena de conexion encriptada no puede superar 2000 caracteres")
    private String encryptedConnectionString;

    private Boolean defaultConnection;

    @Size(max = 100, message = "El usuario creador no puede superar 100 caracteres")
    private String createdBy;
}
