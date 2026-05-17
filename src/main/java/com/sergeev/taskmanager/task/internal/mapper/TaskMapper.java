package com.sergeev.taskmanager.task.internal.mapper;

import com.sergeev.taskmanager.task.api.dto.TaskDto;
import com.sergeev.taskmanager.task.api.dto.TaskShortDto;
import com.sergeev.taskmanager.task.internal.entity.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    Task toEntity(TaskDto dto);

    TaskDto toDto(Task task);

    TaskShortDto toShortDto(Task task);
}
