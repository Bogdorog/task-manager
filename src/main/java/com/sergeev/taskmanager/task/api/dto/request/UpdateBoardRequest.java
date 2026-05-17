package com.sergeev.taskmanager.task.api.dto.request;

public record UpdateBoardRequest(

        Long actorId,
        Long boardId,
        String name,
        String description
) {}
