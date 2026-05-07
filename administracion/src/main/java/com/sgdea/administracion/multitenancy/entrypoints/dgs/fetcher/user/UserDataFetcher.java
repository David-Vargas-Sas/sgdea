package com.sgdea.administracion.multitenancy.entrypoints.dgs.fetcher.user;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.sgdea.administracion.multitenancy.application.dto.user.UserCreateDto;
import com.sgdea.administracion.multitenancy.application.dto.user.UserResponseDto;
import com.sgdea.administracion.multitenancy.application.dto.user.UserUpdateDto;
import com.sgdea.administracion.multitenancy.application.services.UserService;
import com.sgdea.administracion.multitenancy.entrypoints.dgs.input.UserCreateInput;
import com.sgdea.administracion.multitenancy.entrypoints.dgs.input.UserUpdateInput;

import java.util.List;
import java.util.UUID;

@DgsComponent
public class UserDataFetcher {
    private final UserService userService;

    public UserDataFetcher(UserService userService) {
        this.userService = userService;
    }

    @DgsQuery
    public List<UserResponseDto> users() {
        return userService.findAll();
    }

    @DgsQuery
    public UserResponseDto userById(@InputArgument String id) {
        return userService.findById(UUID.fromString(id));
    }

    @DgsQuery
    public UserResponseDto userByUsername(@InputArgument String username) {
        return userService.findByUsername(username);
    }

    @DgsQuery
    public UserResponseDto userByEmail(@InputArgument String email) {
        return userService.findByEmail(email);
    }

    @DgsMutation
    public UserResponseDto createUser(@InputArgument UserCreateInput input) {
        return userService.create(toCreateDto(input));
    }

    @DgsMutation
    public UserResponseDto updateUser(@InputArgument String id, @InputArgument UserUpdateInput input) {
        return userService.update(UUID.fromString(id), toUpdateDto(input));
    }

    @DgsMutation
    public Boolean deleteUser(@InputArgument String id) {
        return userService.delete(UUID.fromString(id));
    }

    private UserCreateDto toCreateDto(UserCreateInput input) {
        UserCreateDto dto = new UserCreateDto();
        dto.setCompanyId(toUuid(input.getCompanyId()));
        dto.setUsername(input.getUsername());
        dto.setEmail(input.getEmail());
        dto.setFullName(input.getFullName());
        dto.setPassword(input.getPassword());
        dto.setRole(input.getRole());
        dto.setCreatedBy(input.getCreatedBy());
        return dto;
    }

    private UserUpdateDto toUpdateDto(UserUpdateInput input) {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setCompanyId(toUuid(input.getCompanyId()));
        dto.setUsername(input.getUsername());
        dto.setEmail(input.getEmail());
        dto.setFullName(input.getFullName());
        dto.setPassword(input.getPassword());
        dto.setRole(input.getRole());
        dto.setActive(input.getActive());
        dto.setUpdatedBy(input.getUpdatedBy());
        return dto;
    }

    private UUID toUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }
}
