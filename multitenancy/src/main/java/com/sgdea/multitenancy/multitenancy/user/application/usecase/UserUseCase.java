package com.sgdea.multitenancy.multitenancy.user.application.usecase;

import com.sgdea.multitenancy.multitenancy.user.application.dto.UserCreateDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserResponseDto;
import com.sgdea.multitenancy.multitenancy.user.application.dto.UserUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserUseCase {
    List<UserResponseDto> findAll();
    UserResponseDto findById(Long id);
    UserResponseDto findByEmail(String email);
    Page<UserResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection);
    UserResponseDto create(UserCreateDto dto);
    UserResponseDto update(Long id, UserUpdateDto dto);
    Boolean delete(Long id);
    String toggleActive(Long id);
}
