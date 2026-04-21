package com.sergeev.taskmanager.user.api.dto;

public record UsersForAdmin(
        Long id,
        String fullName,
        String email,
        String role,
        boolean active,
        Long count
) {}