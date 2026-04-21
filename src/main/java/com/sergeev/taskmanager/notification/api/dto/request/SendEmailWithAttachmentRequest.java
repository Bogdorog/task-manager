package com.sergeev.taskmanager.notification.api.dto.request;

public record SendEmailWithAttachmentRequest(
        String to,
        String subject,
        String text,
        byte[] file,
        String filename
) {}
