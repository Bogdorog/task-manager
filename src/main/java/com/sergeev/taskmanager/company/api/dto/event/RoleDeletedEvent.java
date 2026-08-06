package com.sergeev.taskmanager.company.api.dto.event;

public record RoleDeletedEvent(
        Long companyId,
        String roleName,
        Long actorId,
        String actorName
) {}
