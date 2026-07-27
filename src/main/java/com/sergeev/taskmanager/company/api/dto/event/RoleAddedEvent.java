package com.sergeev.taskmanager.company.api.dto.event;

public record RoleAddedEvent(
        Long companyId,
        String roleName,
        Long actorId,
        String actorName
) {}
