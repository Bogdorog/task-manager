package com.sergeev.taskmanager.task.api.dto;

import java.time.LocalDateTime;

public record TaskDto(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        Long assignedTo,
        Long createdBy,
        Long companyId,
        Long columnId,
        LocalDateTime dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
