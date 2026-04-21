package com.sergeev.taskmanager.user.api.event;

public record PasswordResetRequestedEvent(
        String email,
        String token
) {}