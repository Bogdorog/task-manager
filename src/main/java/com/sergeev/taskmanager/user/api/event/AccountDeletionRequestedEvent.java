package com.sergeev.taskmanager.user.api.event;

public record AccountDeletionRequestedEvent(
        String email,
        String token
) {}
