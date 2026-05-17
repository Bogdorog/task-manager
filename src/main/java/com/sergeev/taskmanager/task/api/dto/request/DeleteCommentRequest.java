package com.sergeev.taskmanager.task.api.dto.request;

public record DeleteCommentRequest(
        Long actorId,
        Long commentId
) {}
