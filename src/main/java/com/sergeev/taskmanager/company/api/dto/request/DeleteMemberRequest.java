package com.sergeev.taskmanager.company.api.dto.request;

public record DeleteMemberRequest(
        Long companyId,
        Long membershipId
) {}
