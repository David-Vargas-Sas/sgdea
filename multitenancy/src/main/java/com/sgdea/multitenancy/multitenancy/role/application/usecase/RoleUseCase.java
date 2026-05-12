package com.sgdea.multitenancy.multitenancy.role.application.usecase;

import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleCreateDto;
import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleResponseDto;
import com.sgdea.multitenancy.multitenancy.role.application.dto.RoleUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoleUseCase {
    List<RoleResponseDto> findAll();
    RoleResponseDto findById(Long id);
    RoleResponseDto findByCode(String code);
    Page<RoleResponseDto> findAllPaginated(int page, int size, String sortBy, String sortDirection);
    RoleResponseDto create(RoleCreateDto dto);
    RoleResponseDto update(Long id, RoleUpdateDto dto);
    Boolean delete(Long id);
    String toggleActive(Long id);
}
