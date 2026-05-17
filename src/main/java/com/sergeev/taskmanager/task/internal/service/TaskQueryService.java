package com.sergeev.taskmanager.task.internal.service;

import com.sergeev.taskmanager.company.api.CheckPermissionApi;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.task.api.dto.*;
import com.sergeev.taskmanager.task.internal.entity.Board;
import com.sergeev.taskmanager.task.internal.entity.BoardColumn;
import com.sergeev.taskmanager.task.internal.entity.Task;
import com.sergeev.taskmanager.task.internal.entity.TaskComment;
import com.sergeev.taskmanager.task.internal.mapper.TaskCommentMapper;
import com.sergeev.taskmanager.task.internal.mapper.TaskMapper;
import com.sergeev.taskmanager.task.internal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskQueryService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskHistoryRepository historyRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository columnRepository;

    private final TaskMapper taskMapper;
    private final TaskCommentMapper commentMapper;

    private final CheckPermissionApi permissionApi;

    // =========================
    // TASKS
    // =========================

    public TaskDto getTask(Long actorId, Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND,
                                "Задача не найдена"
                        )
                );

        permissionApi.checkCanViewTask(actorId, taskId);

        return taskMapper.toDto(task);
    }

    public List<TaskDto> getCompanyTasks(
            Long actorId,
            Long companyId
    ) {

        permissionApi.checkCompanyPermission(
                actorId,
                companyId,
                PermissionEnum.VIEW_TASK.name()
        );

        return taskRepository.findAllByCompanyId(companyId)
                .stream()
                .sorted(
                        Comparator
                                .comparing(Task::getUpdatedAt,
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(
                                        Task::getCreatedAt,
                                        Comparator.reverseOrder()
                                )
                )
                .map(taskMapper::toDto)
                .toList();
    }

    public List<TaskDto> getMyTasks(
            Long actorId,
            Long companyId
    ) {

        permissionApi.checkCompanyPermission(
                actorId,
                companyId,
                PermissionEnum.VIEW_TASK.name()
        );

        return taskRepository
                .findAllByCompanyIdAndAssignedTo(companyId, actorId)
                .stream()
                .sorted(
                        Comparator
                                .comparing(Task::getUpdatedAt,
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(
                                        Task::getCreatedAt,
                                        Comparator.reverseOrder()
                                )
                )
                .map(taskMapper::toDto)
                .toList();
    }

    public List<TaskDto> getCreatedTasks(
            Long actorId,
            Long companyId
    ) {

        permissionApi.checkCompanyPermission(
                actorId,
                companyId,
                PermissionEnum.VIEW_TASK.name()
        );

        return taskRepository
                .findAllByCompanyIdAndCreatedBy(companyId, actorId)
                .stream()
                .sorted(
                        Comparator
                                .comparing(Task::getUpdatedAt,
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(
                                        Task::getCreatedAt,
                                        Comparator.reverseOrder()
                                )
                )
                .map(taskMapper::toDto)
                .toList();
    }

    public List<TaskDto> getColumnTasks(
            Long actorId,
            Long columnId
    ) {

        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND,
                                "Колонка не найдена"
                        )
                );

        permissionApi.checkCompanyPermission(
                actorId,
                column.getBoard().getCompanyId(),
                PermissionEnum.VIEW_TASK.name()
        );

        return taskRepository.findAllByColumnId(columnId)
                .stream()
                .sorted(
                        Comparator
                                .comparing(Task::getUpdatedAt,
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(
                                        Task::getCreatedAt,
                                        Comparator.reverseOrder()
                                )
                )
                .map(taskMapper::toDto)
                .toList();
    }

    // =========================
    // COMMENTS
    // =========================

    public List<TaskCommentDto> getTaskComments(
            Long actorId,
            Long taskId
    ) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND,
                                "Задача не найдена"
                        )
                );

        permissionApi.checkCanViewTask(actorId, taskId);

        return commentRepository.findAllByTaskId(taskId)
                .stream()
                .sorted(
                        Comparator.comparing(TaskComment::getCreatedAt)
                )
                .map(commentMapper::toDto)
                .toList();
    }

    // =========================
    // HISTORY
    // =========================

    public List<?> getTaskHistory(
            Long actorId,
            Long taskId
    ) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND,
                                "Задача не найдена"
                        )
                );

        permissionApi.checkCanViewTask(actorId, taskId);

        return historyRepository.findAllByTaskIdOrderByChangedAtDesc(taskId);
    }

    // =========================
    // BOARDS
    // =========================

    public List<BoardDto> getBoards(
            Long actorId,
            Long companyId
    ) {

        permissionApi.checkCompanyPermission(
                actorId,
                companyId,
                PermissionEnum.VIEW_TASK.name()
        );

        return boardRepository.findAllByCompanyId(companyId)
                .stream()
                .map(this::mapBoard)
                .toList();
    }

    public BoardDto getBoard(
            Long actorId,
            Long boardId
    ) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND,
                                "Доска не найдена"
                        )
                );

        permissionApi.checkCompanyPermission(
                actorId,
                board.getCompanyId(),
                PermissionEnum.VIEW_TASK.name()
        );

        return mapBoard(board);
    }

    // =========================
    // PRIVATE
    // =========================

    private BoardDto mapBoard(Board board) {

        List<BoardColumn> columns = columnRepository
                .findAllByBoardIdOrderByPositionAsc(board.getId());

        Map<Long, List<TaskShortDto>> groupedTasks = taskRepository
                .findAllByBoardId(board.getId())
                .stream()
                .sorted(
                        Comparator
                                .comparing(Task::getUpdatedAt,
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(
                                        Task::getCreatedAt,
                                        Comparator.reverseOrder()
                                )
                )
                .map(taskMapper::toShortDto)
                .collect(Collectors.groupingBy(TaskShortDto::columnId));

        List<BoardColumnDto> columnDtos = columns.stream()
                .map(column -> new BoardColumnDto(
                        column.getId(),
                        column.getName(),
                        column.getPosition(),
                        groupedTasks.getOrDefault(column.getId(), List.of())
                ))
                .toList();

        return new BoardDto(
                board.getId(),
                board.getName(),
                board.getDescription(),
                columnDtos
        );
    }
}