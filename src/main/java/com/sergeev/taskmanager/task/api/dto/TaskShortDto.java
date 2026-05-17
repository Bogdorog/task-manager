package com.sergeev.taskmanager.task.api.dto;

import com.sergeev.taskmanager.task.internal.entity.TaskPriority;

public record TaskShortDto(

        Long id,

        String title,

        TaskPriority priority,

        Long columnId
) {}
