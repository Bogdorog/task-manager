package com.sergeev.taskmanager.company.api.dto.request;

public record InviteUserRequest(
        Long companyId,
        String user,
        Long roleId
) {}
