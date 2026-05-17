package com.sergeev.taskmanager.task.api.dto.request;

public record MoveTaskRequest(

        Long actorId,

        Long taskId,

        Long targetColumnId,

        Long newPosition
) {}
