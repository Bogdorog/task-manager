package com.sergeev.taskmanager.company.api.dto.event;

public record MemberAddedEvent(
        Long companyId,
        String newMemberName,
        String roleName,
        Long actorId,
        String actorName
) {}
