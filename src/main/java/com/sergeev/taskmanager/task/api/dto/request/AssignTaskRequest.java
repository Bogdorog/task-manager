package com.sergeev.taskmanager.task.api.dto.request;

public record AssignTaskRequest(

        Long actorId,
        Long taskId,
        Long assignedUserId
) {}
