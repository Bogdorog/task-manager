package com.sergeev.taskmanager.task.api.event;

import java.time.LocalDateTime;

public record TaskDeadlineOverdueEvent(
        Long taskId,
        String taskTitle,
        Long boardId,
        LocalDateTime dueDate,
        Long creatorId,
        Long assigneeId
) {}