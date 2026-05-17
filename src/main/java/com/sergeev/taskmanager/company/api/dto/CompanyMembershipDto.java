package com.sergeev.taskmanager.company.api.dto;

import java.time.LocalDateTime;

public record CompanyMembershipDto(
        Long id,
        Long userId,
        Long companyId,
        String role,
        LocalDateTime joinedAt
) {}
