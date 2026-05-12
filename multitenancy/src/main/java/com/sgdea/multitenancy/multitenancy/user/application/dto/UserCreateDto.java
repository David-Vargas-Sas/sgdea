package com.sgdea.multitenancy.multitenancy.user.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {
    @NotBlank(message = "El numero de documento es obligatorio")
    @Size(max = 30, message = "El numero de documento no puede superar 30 caracteres")
    private String documentNumber;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    private String password;

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(max = 100, message = "El primer nombre no puede superar 100 caracteres")
    private String firstName;

    @Size(max = 100, message = "El segundo nombre no puede superar 100 caracteres")
    private String secondName;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 100, message = "El primer apellido no puede superar 100 caracteres")
    private String firstLastName;

    @Size(max = 100, message = "El segundo apellido no puede superar 100 caracteres")
    private String secondLastName;

    @Size(max = 30, message = "El telefono no puede superar 30 caracteres")
    private String phone;

    @NotNull(message = "El rol es obligatorio")
    private Long roleId;

    @Size(max = 100, message = "El usuario creador no puede superar 100 caracteres")
    private String createdBy;
}
