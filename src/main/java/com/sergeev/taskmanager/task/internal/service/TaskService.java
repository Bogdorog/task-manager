package com.sergeev.taskmanager.task.internal.service;

import com.sergeev.taskmanager.company.api.CheckPermissionApi;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import com.sergeev.taskmanager.task.api.TaskApi;
import com.sergeev.taskmanager.task.api.dto.TaskCommentDto;
import com.sergeev.taskmanager.task.api.dto.TaskDto;
import com.sergeev.taskmanager.task.api.dto.request.*;
import com.sergeev.taskmanager.task.internal.entity.*;
import com.sergeev.taskmanager.task.internal.mapper.TaskCommentMapper;
import com.sergeev.taskmanager.task.internal.mapper.TaskMapper;
import com.sergeev.taskmanager.task.internal.repository.BoardColumnRepository;
import com.sergeev.taskmanager.task.internal.repository.TaskCommentRepository;
import com.sergeev.taskmanager.task.internal.repository.TaskRepository;
import com.sergeev.taskmanager.user.api.UserApi;
import com.sergeev.taskmanager.user.api.dto.UserShortDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService implements TaskApi {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final UserApi userApi;
    private final BoardColumnRepository columnRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final CheckPermissionApi permissionApi;
    private final TaskHistoryService historyService;
    private final TaskMapper taskMapper;
    private final TaskCommentMapper commentMapper;
    private final SecurityFacadeApi securityFacade;

    // =========================================================
    // CREATE TASK
    // =========================================================

    @Override
    public TaskDto createTask(CreateTaskRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        permissionApi.checkCompanyPermission(
                actorId,
                request.companyId(),
                PermissionEnum.CREATE_TASK.name()
        );

        BoardColumn column = columnRepository.findById(
                request.columnId()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Колонка не найдена"
                )
        );

        validateColumnCompany(
                column,
                request.companyId()
        );

        validateMembershipIfAssigned(
                request.assignedUserId(),
                request.companyId()
        );

        /*Integer nextPosition =
                taskRepository.getNextPositionInColumn(
                        column.getId()
                );*/

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(TaskStatus.BACKLOG)
                .columnId(column.getId())
                .boardId(column.getBoard().getId())
                .createdBy(actorId)
                .assignedTo(request.assignedUserId())
                .dueDate(request.dueDate())
                .createdAt(LocalDateTime.now())
                .build();

        taskRepository.save(task);

        historyService.record(
                task,
                actorId,
                TaskHistoryField.CREATED,
                null,
                task.getTitle()
        );

        historyService.record(
                task,
                actorId,
                TaskHistoryField.STATUS,
                null,
                TaskStatus.BACKLOG.name()
        );

        if (request.assignedUserId() != null) {

            historyService.record(
                    task,
                    actorId,
                    TaskHistoryField.ASSIGNED_TO,
                    null,
                    request.assignedUserId().toString()
            );
        }

        return taskMapper.toDto(task, request.companyId());
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    @Override
    public TaskDto updateTask(UpdateTaskRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        Long companyId = taskRepository.findCompanyIdByTaskId(request.taskId());
        Task task = getTask(request.taskId());

        permissionApi.checkCompanyPermission(
                actorId,
                companyId,
                PermissionEnum.UPDATE_TASK.name()
        );

        updateTitle(task, actorId, request);
        updateDescription(task, actorId, request);
        updatePriority(task, actorId, request);
        updateDueDate(task, actorId, request);
        changeStatus(task, actorId, request);
        assignTask(task, actorId, request);

        task.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toDto(task, companyId);
    }

    // =========================================================
    // ADD COMMENT
    // =========================================================

    @Override
    public TaskCommentDto addComment(AddCommentRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        Task task = getTask(request.taskId());

        UserShortDto assignedTo = userApi.getShortUserById(task.getAssignedTo());
        UserShortDto createdBy = userApi.getShortUserById(task.getCreatedBy());
        TaskDto taskDto = taskMapper.toDto(task, taskRepository.findCompanyIdByTaskId(request.taskId()));

        permissionApi.checkCanViewTask(
                actorId,
                new TaskDto(taskDto.id(), taskDto.title(), taskDto.description(),
                        taskDto.status(), taskDto.priority(), assignedTo, createdBy,
                        taskDto.companyId(), taskDto.columnId(), taskDto.dueDate(),
                        taskDto.createdAt(), taskDto.updatedAt())
        );

        permissionApi.checkCompanyPermission(
                actorId,
                taskDto.companyId(),
                PermissionEnum.COMMENT_TASK.name()
        );

        TaskComment comment = TaskComment.builder()
                .taskId(task.getId())
                .userId(actorId)
                .commentText(request.text())
                .createdAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        historyService.record(
                task,
                actorId,
                TaskHistoryField.COMMENT_ADDED,
                null,
                truncateComment(request.text())
        );

        return commentMapper.toDto(comment);
    }

    // =========================================================
    // DELETE COMMENT
    // =========================================================

    @Override
    public void deleteComment(DeleteCommentRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        TaskComment comment = commentRepository.findById(
                request.commentId()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Комментарий не найден"
                )
        );

        Task task = getTask(comment.getTaskId());

        boolean isAuthor =
                Objects.equals(
                        comment.getUserId(),
                        actorId
                );

        permissionApi.checkCompanyPermission(
                actorId,
                taskRepository.findCompanyIdByTaskId(comment.getTaskId()),
                PermissionEnum.DELETE_COMMENT.name()
        );

        if (!isAuthor) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Недостаточно прав"
            );
        }

        commentRepository.delete(comment);

        historyService.record(
                task,
                actorId,
                TaskHistoryField.COMMENT_DELETED,
                truncateComment(comment.getCommentText()),
                null
        );
    }

    // =========================================================
    // DELETE TASK
    // =========================================================

    @Override
    public void deleteTask(DeleteTaskRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        Task task = getTask(request.taskId());

        permissionApi.checkCompanyPermission(
                actorId,
                taskRepository.findCompanyIdByTaskId(request.taskId()),
                PermissionEnum.DELETE_TASK.name()
        );

        historyService.record(
                task,
                actorId,
                TaskHistoryField.DELETED,
                task.getTitle(),
                null
        );

        taskRepository.delete(task);
    }

    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private Task getTask(Long taskId) {

        return taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Задача не найдена"
                        )
                );
    }

    private void validateColumnCompany(
            BoardColumn column,
            Long companyId
    ) {

        Long actualCompanyId =
                column.getBoard().getCompanyId();

        if (!Objects.equals(actualCompanyId, companyId)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Колонка не принадлежит компании"
            );
        }
    }

    private void validateMembershipIfAssigned(
            Long userId,
            Long companyId
    ) {

        if (userId == null) {
            return;
        }

        boolean exists =
                membershipRepository.existsByUserIdAndCompanyId(
                        userId,
                        companyId
                );

        if (!exists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Исполнитель не состоит в компании"
            );
        }
    }

    private void updateTitle(
            Task task,
            Long actorId,
            UpdateTaskRequest request
    ) {
        if (Objects.equals(
                task.getTitle(),
                request.title()
        )) {
            return;
        }

        historyService.record(
                task,
                actorId,
                TaskHistoryField.TITLE,
                task.getTitle(),
                request.title()
        );

        task.setTitle(request.title());
    }

    private void updateDescription(
            Task task,
            Long actorId,
            UpdateTaskRequest request
    ) {
        if (Objects.equals(
                task.getDescription(),
                request.description()
        )) {
            return;
        }

        historyService.record(
                task,
                actorId,
                TaskHistoryField.DESCRIPTION,
                task.getDescription(),
                request.description()
        );

        task.setDescription(request.description());
    }

    private void updatePriority(
            Task task,
            Long actorId,
            UpdateTaskRequest request
    ) {
        if (task.getPriority() == request.priority()) {
            return;
        }

        historyService.record(
                task,
                actorId,
                TaskHistoryField.PRIORITY,
                task.getPriority().name(),
                request.priority().name()
        );

        task.setPriority(request.priority());
    }

    private void updateDueDate(
            Task task,
            Long actorId,
            UpdateTaskRequest request
    ) {

        if (Objects.equals(
                task.getDueDate(),
                request.dueDate()
        )) {
            return;
        }

        historyService.record(
                task,
                actorId,
                TaskHistoryField.DUE_DATE,
                task.getDueDate() == null
                        ? null
                        : task.getDueDate().toString(),
                request.dueDate() == null
                        ? null
                        : request.dueDate().toString()
        );

        task.setDueDate(request.dueDate());
    }

    private void changeStatus(Task task,
                              Long actorId,
                              UpdateTaskRequest request) {
        if (Objects.equals(
                task.getStatus().name(),
                request.status().name()
        )) {
            return;
        }

        historyService.record(
                task,
                actorId,
                TaskHistoryField.STATUS,
                task.getStatus().name(),
                request.status().name()
        );

        task.setStatus(request.status());
    }

    private void assignTask(Task task,
                            Long actorId,
                            UpdateTaskRequest request) {

        validateMembershipIfAssigned(
                request.assignedToId(),
                taskRepository.findCompanyIdByTaskId(request.taskId())
        );

        Long oldAssigned = task.getAssignedTo();

        historyService.record(
                task,
                actorId,
                TaskHistoryField.ASSIGNED_TO,
                oldAssigned == null
                        ? null
                        : oldAssigned.toString(),
                request.assignedToId() == null
                        ? null
                        : request.assignedToId().toString()
        );

        task.setAssignedTo(request.assignedToId());
    }

    private String truncateComment(String text) {

        if (text == null) {
            return null;
        }

        int maxLength = 120;

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }
}
