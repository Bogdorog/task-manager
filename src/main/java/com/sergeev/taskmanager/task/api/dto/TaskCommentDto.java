package com.sergeev.taskmanager.task.api.dto;

import com.sergeev.taskmanager.user.api.dto.UserShortDto;

import java.time.LocalDateTime;

public record TaskCommentDto(
        Long id,
        Long taskId,
        UserShortDto user,
        String commentText,
        LocalDateTime createdAt
) {}
