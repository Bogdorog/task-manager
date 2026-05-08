package com.sergeev.taskmanager.company.api.dto.request;

public record TransferOwnershipRequest(
        Long companyId,
        Long currentOwnerId,
        Long newOwnerUserId,
        Long newOwnerRoleId
) {}
