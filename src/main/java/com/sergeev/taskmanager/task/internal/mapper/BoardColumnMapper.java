package com.sergeev.taskmanager.task.internal.mapper;

import com.sergeev.taskmanager.task.api.dto.BoardColumnDto;
import com.sergeev.taskmanager.task.internal.entity.BoardColumn;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoardColumnMapper {
    BoardColumnDto toDto(BoardColumn boardColumn);
}
