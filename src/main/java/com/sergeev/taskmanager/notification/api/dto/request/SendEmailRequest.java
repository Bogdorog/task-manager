package com.sergeev.taskmanager.notification.api.dto.request;

public record SendEmailRequest(
        String to,
        String subject,
        String text
) {}
