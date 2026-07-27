package com.sergeev.taskmanager.task.api.event;


import java.time.LocalDateTime;

public record TaskDeadlineApproachingEvent(
        Long taskId,
        Long companyId,
        String taskTitle,
        Long boardId,
        LocalDateTime dueDate,
        Long creatorId,
        Long assigneeId
) {}