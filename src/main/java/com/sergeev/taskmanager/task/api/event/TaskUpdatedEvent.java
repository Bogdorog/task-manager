package com.sergeev.taskmanager.task.api.event;

public record TaskUpdatedEvent(
        Long taskId,
        String taskTitle,
        Long boardId,
        Long companyId,
        Long actorId,
        Long creatorId,
        Long assigneeId
) {}
