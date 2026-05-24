package com.sergeev.taskmanager.company.api.dto;

import com.sergeev.taskmanager.user.api.dto.UserShortDto;

import java.time.LocalDateTime;

public record CompanyMembershipDto(
        Long id,
        UserShortDto user,
        CompanyRoleDto role,
        LocalDateTime joinedAt
) {}