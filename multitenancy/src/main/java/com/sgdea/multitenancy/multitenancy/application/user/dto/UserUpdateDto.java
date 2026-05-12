package com.sgdea.multitenancy.multitenancy.application.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    @Size(max = 30, message = "El numero de documento no puede superar 30 caracteres")
    private String documentNumber;

    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    private String password;

    @Size(max = 100, message = "El primer nombre no puede superar 100 caracteres")
    private String firstName;

    @Size(max = 100, message = "El segundo nombre no puede superar 100 caracteres")
    private String secondName;

    @Size(max = 100, message = "El primer apellido no puede superar 100 caracteres")
    private String firstLastName;

    @Size(max = 100, message = "El segundo apellido no puede superar 100 caracteres")
    private String secondLastName;

    @Size(max = 30, message = "El telefono no puede superar 30 caracteres")
    private String phone;

    private Long roleId;

    private Boolean active;

    @Size(max = 100, message = "El usuario actualizador no puede superar 100 caracteres")
    private String updatedBy;
}
