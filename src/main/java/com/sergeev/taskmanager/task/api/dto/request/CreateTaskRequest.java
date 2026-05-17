package com.sergeev.taskmanager.task.api.dto.request;

import com.sergeev.taskmanager.task.internal.entity.TaskPriority;

import java.time.LocalDateTime;

public record CreateTaskRequest(

        Long actorId,
        Long companyId,

        String title,
        String description,

        TaskPriority priority,

        Long assignedUserId,

        LocalDateTime dueDate
) {}
