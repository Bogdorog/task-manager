package com.sergeev.taskmanager.task.api.dto.request;

public record CreateColumnRequest(

        Long actorId,
        Long boardId,
        String name
) {}
