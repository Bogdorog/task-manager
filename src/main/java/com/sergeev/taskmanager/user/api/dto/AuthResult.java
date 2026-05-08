package com.sergeev.taskmanager.user.api.dto;

public record AuthResult(
        String accessToken,
        String refreshToken,
        UserShortDto user
) {}
