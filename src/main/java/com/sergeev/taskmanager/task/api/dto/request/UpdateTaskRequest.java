package com.sergeev.taskmanager.task.api.dto.request;

import com.sergeev.taskmanager.task.internal.entity.TaskPriority;
import com.sergeev.taskmanager.task.internal.entity.TaskStatus;

import java.time.LocalDateTime;

public record UpdateTaskRequest(

        Long taskId,
        Long assignedToId,
        String title,
        String description,
        TaskPriority priority,
        TaskStatus status,
        LocalDateTime dueDate
) {}
