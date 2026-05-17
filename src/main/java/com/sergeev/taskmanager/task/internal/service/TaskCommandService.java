package com.sergeev.taskmanager.task.internal.service;

import com.sergeev.taskmanager.company.api.CheckPermissionApi;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.task.api.TaskCommandApi;
import com.sergeev.taskmanager.task.api.dto.TaskCommentDto;
import com.sergeev.taskmanager.task.api.dto.TaskDto;
import com.sergeev.taskmanager.task.api.dto.request.*;
import com.sergeev.taskmanager.task.internal.entity.*;
import com.sergeev.taskmanager.task.internal.mapper.TaskCommentMapper;
import com.sergeev.taskmanager.task.internal.mapper.TaskMapper;
import com.sergeev.taskmanager.task.internal.repository.BoardColumnRepository;
import com.sergeev.taskmanager.task.internal.repository.BoardRepository;
import com.sergeev.taskmanager.task.internal.repository.TaskCommentRepository;
import com.sergeev.taskmanager.task.internal.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskCommandService implements TaskCommandApi {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;

    private final BoardRepository boardRepository;
    private final BoardColumnRepository columnRepository;

    private final CompanyMembershipRepository membershipRepository;

    private final CheckPermissionApi permissionApi;
    private final TaskHistoryService historyService;

    private final TaskMapper taskMapper;
    private final TaskCommentMapper commentMapper;

    // =========================================================
    // CREATE TASK
    // =========================================================

    @Override
    public TaskDto createTask(CreateTaskRequest request) {

        permissionApi.checkCompanyPermission(
                request.actorId(),
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

        Integer nextPosition =
                taskRepository.getNextPositionInColumn(
                        column.getId()
                );

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(TaskStatus.OPEN)
                .companyId(request.companyId())
                .columnId(column.getId())
                .createdBy(request.actorId())
                .assignedTo(request.assignedUserId())
                .dueDate(request.dueDate())
                .createdAt(LocalDateTime.now())
                .build();

        taskRepository.save(task);

        historyService.record(
                task,
                request.actorId(),
                TaskHistoryField.CREATED,
                null,
                task.getTitle()
        );

        historyService.record(
                task,
                request.actorId(),
                TaskHistoryField.STATUS,
                null,
                TaskStatus.OPEN.name()
        );

        if (request.assignedUserId() != null) {

            historyService.record(
                    task,
                    request.actorId(),
                    TaskHistoryField.ASSIGNED_TO,
                    null,
                    request.assignedUserId().toString()
            );
        }

        return taskMapper.toDto(task);
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    @Override
    public TaskDto updateTask(UpdateTaskRequest request) {

        Task task = getTask(request.taskId());

        permissionApi.checkCompanyPermission(
                request.actorId(),
                task.getCompanyId(),
                PermissionEnum.UPDATE_TASK.name()
        );

        updateTitle(task, request);
        updateDescription(task, request);
        updatePriority(task, request);
        updateDueDate(task, request);

        task.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toDto(task);
    }

    // =========================================================
    // ASSIGN TASK
    // =========================================================

    @Override
    public TaskDto assignTask(AssignTaskRequest request) {

        Task task = getTask(request.taskId());

        permissionApi.checkCompanyPermission(
                request.actorId(),
                task.getCompanyId(),
                PermissionEnum.ASSIGN_TASK.name()
        );

        validateMembershipIfAssigned(
                request.assignedUserId(),
                task.getCompanyId()
        );

        Long oldAssigned = task.getAssignedTo();

        task.setAssignedTo(request.assignedUserId());
        task.setUpdatedAt(LocalDateTime.now());

        historyService.record(
                task,
                request.actorId(),
                TaskHistoryField.ASSIGNED_TO,
                oldAssigned == null
                        ? null
                        : oldAssigned.toString(),
                request.assignedUserId() == null
                        ? null
                        : request.assignedUserId().toString()
        );

        return taskMapper.toDto(task);
    }

    // =========================================================
    // CHANGE STATUS
    // =========================================================

    @Override
    public TaskDto changeStatus(ChangeTaskStatusRequest request) {

        Task task = getTask(request.taskId());

        permissionApi.checkCanViewTask(
                request.actorId(),
                task.getId()
        );

        TaskStatus oldStatus = task.getStatus();

        if (oldStatus == request.status()) {
            return taskMapper.toDto(task);
        }

        task.setStatus(request.status());
        task.setUpdatedAt(LocalDateTime.now());

        historyService.record(
                task,
                request.actorId(),
                TaskHistoryField.STATUS,
                oldStatus.name(),
                request.status().name()
        );

        return taskMapper.toDto(task);
    }

    // =========================================================
    // ADD COMMENT
    // =========================================================

    @Override
    public TaskCommentDto addComment(AddCommentRequest request) {

        Task task = getTask(request.taskId());

        permissionApi.checkCanViewTask(
                request.actorId(),
                task.getId()
        );

        permissionApi.checkCompanyPermission(
                request.actorId(),
                task.getCompanyId(),
                PermissionEnum.COMMENT_TASK.name()
        );

        TaskComment comment = TaskComment.builder()
                .taskId(task.getId())
                .userId(request.actorId())
                .commentText(request.text())
                .createdAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        historyService.record(
                task,
                request.actorId(),
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
                        request.actorId()
                );

        permissionApi.checkCompanyPermission(
                request.actorId(),
                task.getCompanyId(),
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
                request.actorId(),
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

        Task task = getTask(request.taskId());

        permissionApi.checkCompanyPermission(
                request.actorId(),
                task.getCompanyId(),
                PermissionEnum.DELETE_TASK.name()
        );

        historyService.record(
                task,
                request.actorId(),
                TaskHistoryField.DELETED,
                task.getTitle(),
                null
        );

        taskRepository.delete(task);
    }


    // Utils
    public TaskDto getTaskById(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND,
                                "Задача не найдена"
                        )
                );

        return taskMapper.toDto(task);
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

    private boolean hasPermission(
            Long userId,
            Long companyId,
            PermissionEnum permission
    ) {

        try {

            permissionApi.checkCompanyPermission(
                    userId,
                    companyId,
                    permission.name()
            );

            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    private void updateTitle(
            Task task,
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
                request.actorId(),
                TaskHistoryField.TITLE,
                task.getTitle(),
                request.title()
        );

        task.setTitle(request.title());
    }

    private void updateDescription(
            Task task,
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
                request.actorId(),
                TaskHistoryField.DESCRIPTION,
                task.getDescription(),
                request.description()
        );

        task.setDescription(request.description());
    }

    private void updatePriority(
            Task task,
            UpdateTaskRequest request
    ) {

        if (task.getPriority() == request.priority()) {
            return;
        }

        historyService.record(
                task,
                request.actorId(),
                TaskHistoryField.PRIORITY,
                task.getPriority().name(),
                request.priority().name()
        );

        task.setPriority(request.priority());
    }

    private void updateDueDate(
            Task task,
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
                request.actorId(),
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
