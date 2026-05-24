package com.sergeev.taskmanager.company.api.dto.request;

public record DeleteRoleRequest(
        Long companyId,
        Long roleId
) {}
