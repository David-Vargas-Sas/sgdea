package com.sgdea.administracion.application.dgs.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.sgdea.administracion.application.dgs.input.CompanyCreateInput;
import com.sgdea.administracion.application.dgs.input.CompanyUpdateInput;
import com.sgdea.administracion.domain.dto.company.CompanyCreateDto;
import com.sgdea.administracion.domain.dto.company.CompanyResponseDto;
import com.sgdea.administracion.domain.dto.company.CompanyUpdateDto;
import com.sgdea.administracion.application.service.CompanyService;

import java.util.List;
import java.util.UUID;

@DgsComponent
public class CompanyDataFetcher {
    private final CompanyService companyService;

    public CompanyDataFetcher(CompanyService companyService) {
        this.companyService = companyService;
    }

    @DgsQuery
    public List<CompanyResponseDto> companies() {
        return companyService.findAll();
    }

    @DgsQuery
    public CompanyResponseDto companyById(@InputArgument String id) {
        return companyService.findById(UUID.fromString(id));
    }

    @DgsQuery
    public CompanyResponseDto companyByCode(@InputArgument String code) {
        return companyService.findByCode(code);
    }

    @DgsMutation
    public CompanyResponseDto createCompany(@InputArgument CompanyCreateInput input) {
        return companyService.create(toCreateDto(input));
    }

    @DgsMutation
    public CompanyResponseDto updateCompany(@InputArgument String id, @InputArgument CompanyUpdateInput input) {
        return companyService.update(UUID.fromString(id), toUpdateDto(input));
    }

    @DgsMutation
    public Boolean deleteCompany(@InputArgument String id) {
        return companyService.delete(UUID.fromString(id));
    }

    private CompanyCreateDto toCreateDto(CompanyCreateInput input) {
        CompanyCreateDto dto = new CompanyCreateDto();
        dto.setCode(input.getCode());
        dto.setName(input.getName());
        dto.setNit(input.getNit());
        dto.setDv(input.getDv());
        dto.setLogoPath(input.getLogoPath());
        dto.setCreatedBy(input.getCreatedBy());
        return dto;
    }

    private CompanyUpdateDto toUpdateDto(CompanyUpdateInput input) {
        CompanyUpdateDto dto = new CompanyUpdateDto();
        dto.setCode(input.getCode());
        dto.setName(input.getName());
        dto.setNit(input.getNit());
        dto.setDv(input.getDv());
        dto.setLogoPath(input.getLogoPath());
        dto.setActive(input.getActive());
        dto.setUpdatedBy(input.getUpdatedBy());
        return dto;
    }
}
