package com.sergeev.taskmanager.task.api.dto.request;

public record AssignTaskRequest(

        Long taskId,
        Long assignedUserId
) {}
