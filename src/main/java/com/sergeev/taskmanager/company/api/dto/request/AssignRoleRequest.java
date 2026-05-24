package com.sergeev.taskmanager.company.api.dto.request;

public record AssignRoleRequest(
        Long companyId,
        Long membershipId,
        Long roleId
) {}
