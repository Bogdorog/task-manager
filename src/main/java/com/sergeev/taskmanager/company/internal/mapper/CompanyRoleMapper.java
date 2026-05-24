package com.sergeev.taskmanager.company.internal.mapper;

import com.sergeev.taskmanager.company.api.dto.CompanyRoleDto;
import com.sergeev.taskmanager.company.internal.entity.CompanyRole;
import com.sergeev.taskmanager.company.internal.entity.Permission;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CompanyRoleMapper {

    CompanyRoleDto toDto(CompanyRole role);

    // Вспомогательные методы для конвертации прав
    default PermissionEnum mapPermission(Permission permission) {
        if (permission == null) return null;
        return PermissionEnum.valueOf(permission.getName());
    }

    default Set<PermissionEnum> mapPermissions(Set<Permission> permissions) {
        if (permissions == null) return null;
        return permissions.stream()
                .map(this::mapPermission)
                .collect(Collectors.toSet());
    }
}
