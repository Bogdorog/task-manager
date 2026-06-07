package com.sergeev.taskmanager.company.api.dto;

public record ShortCompanyMembershipDto(
        Long id,
        String name,
        String role
) {}