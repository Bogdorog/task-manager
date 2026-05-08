package com.sergeev.taskmanager.company.api.dto.request;

public record DeleteRoleRequest(
        Long actorId,
        Long companyId,
        Long roleId
) {}
