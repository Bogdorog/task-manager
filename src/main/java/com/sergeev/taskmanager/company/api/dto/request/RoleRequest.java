package com.sergeev.taskmanager.company.api.dto.request;

import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;

import java.util.Set;

public record RoleRequest(
        Long actorId,
        Long companyId,
        String name,
        String description,
        Set<PermissionEnum> permissions
) {}
