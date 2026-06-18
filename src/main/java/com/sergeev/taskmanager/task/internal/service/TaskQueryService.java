package com.sergeev.taskmanager.task.internal.service;

import com.sergeev.taskmanager.company.api.CheckPermissionApi;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import com.sergeev.taskmanager.task.api.dto.*;
import com.sergeev.taskmanager.task.internal.entity.Board;
import com.sergeev.taskmanager.task.internal.entity.BoardColumn;
import com.sergeev.taskmanager.task.internal.entity.Task;
import com.sergeev.taskmanager.task.internal.entity.TaskComment;
import com.sergeev.taskmanager.task.internal.mapper.TaskCommentMapper;
import com.sergeev.taskmanager.task.internal.mapper.TaskMapper;
import com.sergeev.taskmanager.task.internal.repository.*;
import com.sergeev.taskmanager.user.api.UserApi;
import com.sergeev.taskmanager.user.api.dto.UserShortDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final UserApi userApi;
    private final TaskMapper taskMapper;
    private final TaskCommentMapper commentMapper;
    private final SecurityFacadeApi securityFacadeApi;
    private final CheckPermissionApi permissionApi;

    // =========================
    // TASKS — SINGLE
    // =========================

    public TaskDto getTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Задача не найдена"));

        TaskDto base = taskMapper.toDto(task, taskRepository.findCompanyIdByTaskId(task.getId()));
        return enrichTaskWithUsers(task, base);
    }

    /**
     * Получить все задачи компании
     */
    public List<TaskDto> getCompanyTasks(Long companyId) {

        List<TaskDto> myTasks = getMyTasks(companyId);
        List<TaskDto> createdTasks = getCreatedTasks(companyId);
        List<TaskDto> otherTasks = getOtherTasks(companyId);
        List<TaskDto> preResult = Stream.concat(myTasks.stream(), createdTasks.stream())
                .distinct()
                .collect(Collectors.toList());
        if (otherTasks != null && !otherTasks.isEmpty()) {
            return Stream.concat(preResult.stream(), otherTasks.stream())
                    .distinct()
                    .collect(Collectors.toList());
        } else return preResult;
    }

    /**
     * Получить задачи компании, где текущий пользователь является исполнителем
     */
    public List<TaskDto> getMyTasks(Long companyId) {
        Long actorId = securityFacadeApi.getCurrentUserId();
        List<Task> tasks = taskRepository.findAllByCompanyIdAndAssignedTo(companyId, actorId);
        return enrichTasksWithUsers(tasks);
    }

    /**
     * Получить задачи компании, которые текущий пользователь лично создал
     */
    public List<TaskDto> getCreatedTasks(Long companyId) {
        Long actorId = securityFacadeApi.getCurrentUserId();
        List<Task> tasks = taskRepository.findAllByCompanyIdAndCreatedBy(companyId, actorId);
        return enrichTasksWithUsers(tasks);
    }

    /**
     * Получить задачи компании, которые не относятся к текущему пользователю
     */
    private List<TaskDto> getOtherTasks(Long companyId) {

        try {
            permissionApi.checkCompanyPermission(
                    securityFacadeApi.getCurrentUserId(),
                    companyId,
                    PermissionEnum.VIEW_ALL_TASKS.name()
            );
        } catch (AccessDeniedException e)
        {
            return null;
        }
        List<Task> tasks = taskRepository.findAllByCompanyId(companyId);
        return enrichTasksWithUsers(tasks);
    }

    // =========================
    // COMMENTS — с обогащением пользователя
    // =========================

    public List<TaskCommentDto> getTaskComments(Long taskId) {
        List<TaskComment> comments = commentRepository.findAllByTaskId(taskId);
        // Пакетная загрузка авторов комментариев
        Set<Long> userIds = comments.stream()
                .map(TaskComment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> usersMap = userApi.getUsersByIds(userIds);

        return comments.stream()
                .sorted(Comparator.comparing(TaskComment::getCreatedAt))
                .map(comment -> {
                    TaskCommentDto base = commentMapper.toDto(comment);
                    UserShortDto user = comment.getUserId() != null ? usersMap.get(comment.getUserId()) : null;
                    return new TaskCommentDto(
                            base.id(), base.taskId(), user,
                            base.commentText(), base.createdAt()
                    );
                })
                .toList();
    }

    // =========================
    // HISTORY
    // =========================

    public List<?> getTaskHistory(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Задача не найдена"));

        permissionApi.checkCanViewTask(securityFacadeApi.getCurrentUserId(), enrichTaskWithUsers(task, taskMapper.toDto(task, taskRepository.findCompanyIdByTaskId(task.getId()))));

        return historyRepository.findAllByTaskIdOrderByChangedAtDesc(taskId);
    }

    // =========================
    // BOARDS
    // =========================

    public List<BoardDto> getBoards( Long companyId) {
        //permissionApi.checkCompanyPermission(securityFacadeApi.getCurrentUserId(), companyId, PermissionEnum.VIEW_TASK.name());

        return boardRepository.findAllByCompanyId(companyId)
                .stream()
                .map(this::mapBoard)
                .toList();
    }

    public BoardDto getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Доска не найдена"));

        /*permissionApi.checkCompanyPermission(
                securityFacadeApi.getCurrentUserId(),
                board.getCompanyId(),
                PermissionEnum.VIEW_TASK.name()
        );*/

        return mapBoard(board);
    }

    // =========================
    // PRIVATE HELPERS
    // =========================

    /**
     * Пакетное обогащение списка задач данными пользователей.
     * Избегает N+1: один запрос за всеми пользователями.
     */
    private List<TaskDto> enrichTasksWithUsers(List<Task> tasks) {
        if (tasks.isEmpty()) return List.of();

        // Собираем все уникальные ID пользователей
        Set<Long> userIds = tasks.stream()
                .flatMap(t -> Stream.of(t.getAssignedTo(), t.getCreatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Один запрос за всеми пользователями
        Map<Long, UserShortDto> usersMap = userApi.getUsersByIds(userIds);

        // Маппинг + обогащение
        return tasks.stream()
                .sorted(Comparator
                        .comparing(Task::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Task::getCreatedAt, Comparator.reverseOrder()))
                .map(task -> {
                    TaskDto base = taskMapper.toDto(task, taskRepository.findCompanyIdByTaskId(task.getId()));
                    return enrichTaskWithUsers(task, base, usersMap);
                })
                .toList();
    }

    /**
     * Обогащение одной задачи (использует пакетный маппинг, если передан usersMap).
     */
    private TaskDto enrichTaskWithUsers(Task task, TaskDto base) {
        // Для одиночной задачи — два отдельных запроса
        UserShortDto assignedTo = task.getAssignedTo() != null
                ? userApi.getShortUserById(task.getAssignedTo())
                : null;
        UserShortDto createdBy = task.getCreatedBy() != null
                ? userApi.getShortUserById(task.getCreatedBy())
                : null;

        return new TaskDto(
                base.id(), base.title(), base.description(), base.status(),
                base.priority(), assignedTo, createdBy, base.companyId(),
                base.columnId(), base.dueDate(), base.createdAt(), base.updatedAt()
        );
    }

    /**
     * Перегрузка для пакетного обогащения (используется внутри enrichTasksWithUsers).
     */
    private TaskDto enrichTaskWithUsers(Task task, TaskDto base, Map<Long, UserShortDto> usersMap) {
        return new TaskDto(
                base.id(), base.title(), base.description(), base.status(),
                base.priority(),
                task.getAssignedTo() != null ? usersMap.get(task.getAssignedTo()) : null,
                task.getCreatedBy() != null ? usersMap.get(task.getCreatedBy()) : null,
                base.companyId(), base.columnId(), base.dueDate(),
                base.createdAt(), base.updatedAt()
        );
    }

    /**
     * Маппинг доски с обогащением задач.
     */
    private BoardDto mapBoard(Board board) {
        List<BoardColumn> columns = columnRepository
                .findAllByBoardIdOrderByPositionAsc(board.getId());
        Long actorId = securityFacadeApi.getCurrentUserId();
        List<Task> tasks = List.of();
        if (permissionApi.checkCanViewTasks(actorId, board.getCompanyId()))
        {
            tasks = taskRepository.findAllByBoardId(board.getId());
        } else {
            List<Task> myTasks = taskRepository.findAllByCompanyIdAndAssignedTo(board.getCompanyId(), actorId);
            List<Task> createdTasks = taskRepository.findAllByCompanyIdAndCreatedBy(board.getCompanyId(), actorId);
            tasks = Stream.concat(myTasks.stream(), createdTasks.stream())
                    .distinct()
                    .collect(Collectors.toList());
        }

        // Пакетное обогащение задач
        Set<Long> userIds = tasks.stream()
                .flatMap(t -> Stream.of(t.getAssignedTo(), t.getCreatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserShortDto> usersMap = userApi.getUsersByIds(userIds);

        // Группировка задач по колонкам с обогащением
        Map<Long, List<TaskShortDto>> groupedTasks = tasks.stream()
                .sorted(Comparator
                        .comparing(Task::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Task::getCreatedAt, Comparator.reverseOrder()))
                .map(task -> {
                    TaskShortDto base = taskMapper.toShortDto(task);
                    UserShortDto assignedTo = task.getAssignedTo() != null
                            ? usersMap.get(task.getAssignedTo())
                            : null;
                    return new TaskShortDto(
                            base.id(), base.title(), base.priority(),
                            base.status(), assignedTo, base.columnId()
                    );
                })
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