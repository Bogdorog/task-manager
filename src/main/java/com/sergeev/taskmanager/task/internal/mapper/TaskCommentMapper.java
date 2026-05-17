package com.sergeev.taskmanager.task.internal.mapper;

import com.sergeev.taskmanager.task.api.dto.TaskCommentDto;
import com.sergeev.taskmanager.task.internal.entity.TaskComment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskCommentMapper {
    TaskComment toEntity(TaskCommentDto dto);

    TaskCommentDto toDto(TaskComment comment);
}
