package com.sgdea.multitenancy.multitenancy.auth.application.usecase;

import com.sgdea.multitenancy.multitenancy.auth.application.dto.AuthLoginRequestDto;
import com.sgdea.multitenancy.multitenancy.auth.application.dto.AuthLoginResponseDto;
import com.sgdea.multitenancy.multitenancy.auth.application.dto.AuthLogoutRequestDto;
import com.sgdea.multitenancy.multitenancy.auth.application.dto.AuthRefreshRequestDto;

public interface AuthUseCase {
    AuthLoginResponseDto login(AuthLoginRequestDto dto);

    AuthLoginResponseDto refresh(AuthRefreshRequestDto dto);

    Boolean logout(AuthLogoutRequestDto dto);

    Boolean logout(String token);
}
