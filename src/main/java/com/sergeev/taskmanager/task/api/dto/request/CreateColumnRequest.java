package com.sergeev.taskmanager.task.api.dto.request;

public record CreateColumnRequest(

        Long boardId,
        String name
) {}
