package com.sergeev.taskmanager.task.api.dto.request;

public record UpdateColumnRequest(
        Long columnId,
        String name
) {}
