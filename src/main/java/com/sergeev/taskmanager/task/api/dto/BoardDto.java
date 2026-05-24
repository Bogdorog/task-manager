package com.sergeev.taskmanager.task.api.dto;

import java.util.List;

public record BoardDto(
        Long id,
        String name,
        String description,
        List<BoardColumnDto> columns
) {}
