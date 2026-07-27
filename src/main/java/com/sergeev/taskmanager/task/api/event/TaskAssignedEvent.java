package com.sergeev.taskmanager.task.api.event;

public record TaskAssignedEvent(
        Long taskId,
        String taskTitle,
        Long boardId,
        Long companyId,
        Long actorId,
        Long newAssigneeId
) {}
