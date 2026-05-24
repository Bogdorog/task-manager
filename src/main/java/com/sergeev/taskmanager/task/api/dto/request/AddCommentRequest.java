package com.sergeev.taskmanager.task.api.dto.request;

public record AddCommentRequest(
        Long taskId,
        String text
) {}
