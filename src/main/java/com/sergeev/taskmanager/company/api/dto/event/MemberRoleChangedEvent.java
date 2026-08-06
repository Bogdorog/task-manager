package com.sergeev.taskmanager.company.api.dto.event;

public record MemberRoleChangedEvent(
        Long companyId,
        String memberName,
        String roleName,
        Long actorId,
        String actorName
) {}
