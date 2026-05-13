package com.sgdea.multitenancy.multitenancy.authAudit.application.mapper;

import com.sgdea.multitenancy.multitenancy.authAudit.application.dto.AuthAuditResponseDto;
import com.sgdea.multitenancy.multitenancy.authAudit.domain.model.AuthAudit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthAuditMapper {

    // ENTITY -> RESPONSE
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    AuthAuditResponseDto toResponseDTO(AuthAudit entity);
}
