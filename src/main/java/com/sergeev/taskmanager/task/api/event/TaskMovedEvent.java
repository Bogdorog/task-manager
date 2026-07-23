package com.sergeev.taskmanager.task.api.event;

public record TaskMovedEvent(
        Long taskId,
        String taskTitle,
        Long boardId,
        Long fromColumnId,
        String fromColumnName,
        Long toColumnId,
        String toColumnName,
        Long actorId,
        Long creatorId,
        Long assigneeId
) {}