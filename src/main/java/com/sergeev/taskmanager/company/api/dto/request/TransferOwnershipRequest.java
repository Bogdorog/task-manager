package com.sergeev.taskmanager.company.api.dto.request;

public record TransferOwnershipRequest(
        Long companyId,
        Long newOwnerUserId,
        Long newOwnerRoleId
) {}
