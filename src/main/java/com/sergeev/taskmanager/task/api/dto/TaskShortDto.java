package com.sergeev.taskmanager.task.api.dto;

import com.sergeev.taskmanager.task.internal.entity.TaskPriority;
import com.sergeev.taskmanager.task.internal.entity.TaskStatus;
import com.sergeev.taskmanager.user.api.dto.UserShortDto;

public record TaskShortDto(
        Long id,
        String title,
        TaskPriority priority,
        TaskStatus status,
        UserShortDto assignedTo,
        Long columnId
) {}
