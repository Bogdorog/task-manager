package com.sergeev.taskmanager.task.api.dto;

import java.util.List;

public record BoardColumnDto(

        Long id,

        String name,

        Integer position,

        List<TaskShortDto> tasks
) {}
