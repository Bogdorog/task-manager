package com.sergeev.taskmanager.task.api.dto.request;

public record AddCommentRequest(

        Long actorId,
        Long taskId,

        String text
) {}
