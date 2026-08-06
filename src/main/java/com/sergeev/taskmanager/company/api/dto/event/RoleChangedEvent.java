package com.sergeev.taskmanager.company.api.dto.event;

public record RoleChangedEvent(
        Long companyId,
        String roleName,
        Long actorId,
        String actorName
) {}
