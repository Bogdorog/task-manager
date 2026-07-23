package com.sergeev.taskmanager.task.api.event;

public record TaskDeletedEvent(
        Long taskId,
        String taskTitle,
        Long boardId,
        Long actorId,
        Long creatorId,
        Long assigneeId
) {}