package com.sergeev.taskmanager.task.api;

import com.sergeev.taskmanager.task.api.dto.BoardColumnDto;
import com.sergeev.taskmanager.task.api.dto.BoardDto;
import com.sergeev.taskmanager.task.api.dto.request.CreateBoardRequest;
import com.sergeev.taskmanager.task.api.dto.request.CreateColumnRequest;
import com.sergeev.taskmanager.task.api.dto.request.MoveColumnRequest;
import com.sergeev.taskmanager.task.api.dto.request.UpdateBoardRequest;

public interface BoardApi {

    BoardDto createBoard(CreateBoardRequest request);

    BoardDto updateBoard(UpdateBoardRequest request);

    void deleteBoard(Long actorId, Long boardId);

    BoardDto getBoard(Long actorId, Long boardId);

    BoardColumnDto createColumn(CreateColumnRequest request);

    BoardColumnDto moveColumn(MoveColumnRequest request);

    void deleteColumn(Long actorId, Long columnId);
}
