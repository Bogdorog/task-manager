package com.sergeev.taskmanager.company.api.dto.event;

public record MemberLeftEvent(
        Long companyId,
        String actorName,
        String roleName
) {}

