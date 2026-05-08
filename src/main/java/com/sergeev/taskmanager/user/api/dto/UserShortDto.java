package com.sergeev.taskmanager.user.api.dto;

public record UserShortDto(
        Long id,
        String login,
        String fullName,
        String role
) {}
