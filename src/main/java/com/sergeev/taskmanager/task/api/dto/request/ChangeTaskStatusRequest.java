package com.sergeev.taskmanager.task.api.dto.request;

import com.sergeev.taskmanager.task.internal.entity.TaskStatus;

public record ChangeTaskStatusRequest(

        Long taskId,
        TaskStatus status
) {}
