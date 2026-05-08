package com.sergeev.taskmanager.company.api.dto.request;

public record LeaveCompanyRequest(
        Long actorId,
        Long companyId
) {}
