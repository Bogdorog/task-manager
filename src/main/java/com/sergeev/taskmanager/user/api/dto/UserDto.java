package com.sergeev.taskmanager.user.api.dto;

public record UserDto(
        Long id,
        String login,
        String fullName,
        String phone,
        String email,
        String address,
        String role,
        String avatarUrl
) {}
