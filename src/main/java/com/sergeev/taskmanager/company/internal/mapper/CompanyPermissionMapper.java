package com.sergeev.taskmanager.company.internal.mapper;

import com.sergeev.taskmanager.company.api.dto.CompanyPermissionsDto;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CompanyPermissionMapper {
    public CompanyPermissionsDto toDto(Set<PermissionEnum> permissions) {

        return new CompanyPermissionsDto(
                permissions.contains(PermissionEnum.CREATE_TASK),
                permissions.contains(PermissionEnum.UPDATE_TASK),
                permissions.contains(PermissionEnum.DELETE_TASK),
                permissions.contains(PermissionEnum.ASSIGN_TASK),
                permissions.contains(PermissionEnum.VIEW_TASK),
                permissions.contains(PermissionEnum.VIEW_ALL_TASKS),
                permissions.contains(PermissionEnum.INVITE_USER),
                permissions.contains(PermissionEnum.VIEW_MEMBERS),
                permissions.contains(PermissionEnum.MANAGE_MEMBERS),
                permissions.contains(PermissionEnum.VIEW_ROLES),
                permissions.contains(PermissionEnum.MANAGE_ROLES),
                permissions.contains(PermissionEnum.MANAGE_COMPANY),
                permissions.contains(PermissionEnum.MANAGE_BOARDS)
        );
    }

    public CompanyPermissionsDto ownerToDto() {
        return new CompanyPermissionsDto(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true
        );
    }
}
