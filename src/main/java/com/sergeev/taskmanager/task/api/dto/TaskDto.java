package com.sergeev.taskmanager.task.api.dto;

import com.sergeev.taskmanager.user.api.dto.UserShortDto;

import java.time.LocalDateTime;

public record TaskDto(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        UserShortDto assignedTo,
        UserShortDto createdBy,
        Long companyId,
        Long columnId,
        LocalDateTime dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
