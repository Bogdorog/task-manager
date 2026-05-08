package com.sergeev.taskmanager.company.api.dto.request;

public record CreateRoleRequest(
        Long actorId,
        Long companyId,
        String name,
        String description
) {}
