package com.sergeev.taskmanager.task.api.dto.request;

import com.sergeev.taskmanager.task.internal.entity.TaskPriority;

import java.time.LocalDateTime;

public record UpdateTaskRequest(

        Long taskId,
        String title,
        String description,
        TaskPriority priority,
        LocalDateTime dueDate
) {}
