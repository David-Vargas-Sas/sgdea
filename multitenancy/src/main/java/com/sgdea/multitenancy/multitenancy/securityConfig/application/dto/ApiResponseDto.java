package com.sgdea.multitenancy.multitenancy.securityConfig.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private Map<String, String> errors;

    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data, null);
    }

    public static <T> ApiResponseDto<T> error(String message, Map<String, String> errors) {
        return new ApiResponseDto<>(false, message, null, errors);
    }
}
