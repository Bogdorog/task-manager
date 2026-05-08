package com.sergeev.taskmanager.company.api.dto.request;

public record AssignRoleRequest(
        Long actorId,
        Long companyId,
        Long membershipId,
        Long roleId
) {}
