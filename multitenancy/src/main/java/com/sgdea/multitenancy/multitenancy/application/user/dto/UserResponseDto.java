package com.sgdea.multitenancy.multitenancy.application.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String documentNumber;
    private String email;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private String phone;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Boolean active;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
