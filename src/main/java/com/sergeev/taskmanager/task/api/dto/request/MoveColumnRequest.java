package com.sergeev.taskmanager.task.api.dto.request;

public record MoveColumnRequest(

        Long actorId,

        Long boardId,

        Long columnId,

        Integer newIndex
) {}
