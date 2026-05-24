package com.sergeev.taskmanager.task.internal.mapper;

import com.sergeev.taskmanager.task.api.dto.TaskDto;
import com.sergeev.taskmanager.task.api.dto.TaskShortDto;
import com.sergeev.taskmanager.task.internal.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    // Для обратной конвертации (если нужно)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Task toEntity(TaskDto dto);

    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    TaskDto toDto(Task task);

    @Mapping(target = "assignedTo", ignore = true)
    TaskShortDto toShortDto(Task task);


}
