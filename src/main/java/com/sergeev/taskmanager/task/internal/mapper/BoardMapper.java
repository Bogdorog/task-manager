package com.sergeev.taskmanager.task.internal.mapper;

import com.sergeev.taskmanager.task.api.dto.BoardDto;
import com.sergeev.taskmanager.task.internal.entity.Board;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = {TaskMapper.class, BoardColumnMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardMapper {
    BoardDto toDto(Board board);
}
