package com.sergeev.taskmanager.task.api.dto;

import com.sergeev.taskmanager.task.internal.entity.Task;
import com.sergeev.taskmanager.user.internal.entity.User;

import java.time.LocalDateTime;

public record TaskCommentDto(
        Long id,
        Task task,
        User user,
        String commentText,
        LocalDateTime createdAt
) {}
