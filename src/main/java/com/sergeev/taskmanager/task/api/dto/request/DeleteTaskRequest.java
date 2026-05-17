package com.sergeev.taskmanager.task.api.dto.request;

public record DeleteTaskRequest(
        Long actorId,
        Long taskId
) {}
