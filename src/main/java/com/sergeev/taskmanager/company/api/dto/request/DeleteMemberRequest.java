package com.sergeev.taskmanager.company.api.dto.request;

public record DeleteMemberRequest(
        Long actorId,
        Long companyId,
        Long membershipId
) {}
