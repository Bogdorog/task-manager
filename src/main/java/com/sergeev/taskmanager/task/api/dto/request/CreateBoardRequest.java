package com.sergeev.taskmanager.task.api.dto.request;

public record CreateBoardRequest(

        Long actorId,
        Long companyId,
        String name,
        String description
) {}
