package com.sergeev.taskmanager.company.api.dto;

import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;

import java.util.Set;

public record CompanyRoleDto(
        Long id,
        String name,
        String description,
        Set<PermissionEnum> permissions
) {}
