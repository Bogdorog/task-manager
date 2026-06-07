package com.sergeev.taskmanager.company.internal.mapper;

import com.sergeev.taskmanager.company.api.dto.CompanyMembershipDto;
import com.sergeev.taskmanager.company.api.dto.ShortCompanyMembershipDto;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import com.sergeev.taskmanager.company.internal.entity.Permission;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CompanyMembershipMapper {
    @Mapping(target = "user", ignore = true)
    CompanyMembershipDto toDto (CompanyMembership membership);

    @Mapping(target = "name", ignore = true)
    @Mapping(target = "role", expression = "java(membership.getRole().getName())")
    ShortCompanyMembershipDto toShortDto (CompanyMembership membership);

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
