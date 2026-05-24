package com.sergeev.taskmanager.task.api.dto.request;

public record CreateBoardRequest(

        Long companyId,
        String name,
        String description
) {}
