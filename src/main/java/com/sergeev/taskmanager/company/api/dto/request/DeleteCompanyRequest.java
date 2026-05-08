package com.sergeev.taskmanager.company.api.dto.request;

public record DeleteCompanyRequest(
        Long actorId,
        Long companyId
) {}
