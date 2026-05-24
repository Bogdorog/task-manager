package com.sergeev.taskmanager.company.api.dto.request;

public record InviteUserRequest(
        Long companyId,
        Long userId,
        Long roleId
) {}
