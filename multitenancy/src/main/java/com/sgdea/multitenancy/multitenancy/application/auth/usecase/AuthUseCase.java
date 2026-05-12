package com.sgdea.multitenancy.multitenancy.application.auth.usecase;

import com.sgdea.multitenancy.multitenancy.application.auth.dto.AuthLoginRequestDto;
import com.sgdea.multitenancy.multitenancy.application.auth.dto.AuthLoginResponseDto;
import com.sgdea.multitenancy.multitenancy.application.auth.dto.AuthLogoutRequestDto;
import com.sgdea.multitenancy.multitenancy.application.auth.dto.AuthRefreshRequestDto;

public interface AuthUseCase {
    AuthLoginResponseDto login(AuthLoginRequestDto dto);

    AuthLoginResponseDto refresh(AuthRefreshRequestDto dto);

    Boolean logout(AuthLogoutRequestDto dto);

    Boolean logout(String token);
}
