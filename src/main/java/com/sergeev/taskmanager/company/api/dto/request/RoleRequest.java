package com.sergeev.taskmanager.company.api.dto.request;

import com.sergeev.taskmanager.company.api.PermissionEnum;

import java.util.Set;

public record RoleRequest(
        Long companyId,
        String name,
        String description,
        Set<PermissionEnum> permissions
) {}
