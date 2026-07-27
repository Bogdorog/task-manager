package com.sergeev.taskmanager.task.internal.controller;

import com.sergeev.taskmanager.task.api.dto.BoardDto;
import com.sergeev.taskmanager.task.api.dto.TaskCommentDto;
import com.sergeev.taskmanager.task.api.dto.TaskDto;
import com.sergeev.taskmanager.task.api.dto.request.*;
import com.sergeev.taskmanager.task.internal.service.BoardService;
import com.sergeev.taskmanager.task.internal.service.TaskQueryService;
import com.sergeev.taskmanager.task.internal.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task", description = "Доски и задачи")
public class TaskController {

    private final TaskService commandService;
    private final TaskQueryService queryService;
    private final BoardService boardService;

    // =========================================================
    // Управление задачей
    // =========================================================

    @PostMapping
    @Operation(summary = "Создание задачи")
    public TaskDto createTask(
            @RequestBody CreateTaskRequest request
    ) {
        return commandService.createTask(request);
    }

    @PutMapping("/{taskId}")
    public TaskDto updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request
    ) {

        UpdateTaskRequest updatedRequest = new UpdateTaskRequest(
                taskId,
                request.assignedToId(),
                request.title(),
                request.description(),
                request.priority(),
                request.status(),
                request.dueDate()
        );

        return commandService.updateTask(updatedRequest);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Удаление задачи")
    public void deleteTask(
            @PathVariable Long taskId
    ) {

        DeleteTaskRequest updatedRequest =
                new DeleteTaskRequest(taskId);

        commandService.deleteTask(updatedRequest);
    }

    @PatchMapping("/{taskId}/move")
    @Operation(summary = "Перемещение задачи между колонками")
    public void moveTask(
            @PathVariable Long taskId,
            @RequestBody MoveTaskRequest request
    ) {

        MoveTaskRequest updatedRequest =
                new MoveTaskRequest(
                        taskId,
                        request.newColumnId()
                );

        boardService.moveTask(updatedRequest);
    }

    // =========================================================
    // Управление комментарием
    // =========================================================

    @PostMapping("/{taskId}/comments")
    @Operation(summary = "Создание комментария")
    public TaskCommentDto addComment(
            @PathVariable Long taskId,
            @RequestBody AddCommentRequest request
    ) {

        AddCommentRequest updatedRequest =
                new AddCommentRequest(
                        taskId,
                        request.text());

        return commandService.addComment(updatedRequest);
    }

    @DeleteMapping("/{taskId}/comments/{commentId}")
    @Operation(summary = "Удаление комментария")
    public void deleteComment(
            @PathVariable Long commentId
    ) {

        DeleteCommentRequest updatedRequest =
                new DeleteCommentRequest(
                        commentId
                );

        commandService.deleteComment(updatedRequest);
    }

    // =========================================================
    // Различные списки задач
    // =========================================================

    @GetMapping("/{taskId}")
    @Operation(summary = "Получение одной задачи")
    public TaskDto getTask(
            @PathVariable Long taskId
    ) {
        return queryService.getTask(taskId);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Получение всех задач компании")
    public List<TaskDto> getCompanyTasks(
            @PathVariable Long companyId
    ) {
        return queryService.getCompanyTasks(companyId);
    }

    @GetMapping("/company/{companyId}/my")
    @Operation(summary = "Получение всех задач, где пользователь исполнитель")
    public List<TaskDto> getMyTasks(
            @PathVariable Long companyId
    ) {
        return queryService.getMyTasks(companyId);
    }

    @GetMapping("/company/{companyId}/created")
    @Operation(summary = "Получение всех задач, где пользователь создатель")
    public List<TaskDto> getCreatedTasks(
            @PathVariable Long companyId
    ) {
        return queryService.getCreatedTasks(companyId);
    }

    @GetMapping("/{taskId}/comments")
    @Operation(summary = "Получение всех комментариев задачи")
    public List<TaskCommentDto> getComments(
            @PathVariable Long taskId
    ) {
        return queryService.getTaskComments(taskId);
    }

    @GetMapping("/{taskId}/history")
    @Operation(summary = "Получение истории изменения задачи")
    public List<?> getTaskHistory(
            @PathVariable Long taskId
    ) {
        return queryService.getTaskHistory(taskId);
    }

    // =========================================================
    // Управление досками
    // =========================================================

    @PostMapping("/boards")
    @Operation(summary = "Создание доски")
    public BoardDto createBoard(
            @RequestBody CreateBoardRequest request
    ) {
        return boardService.createBoard(request);
    }

    @PutMapping("/boards/{boardId}")
    @Operation(summary = "Изменение доски")
    public BoardDto updateBoard(
            @PathVariable Long boardId,
            @RequestBody UpdateBoardRequest request
    ) {

        UpdateBoardRequest updatedRequest =
                new UpdateBoardRequest(
                        boardId,
                        request.name(),
                        request.description()
                );

        return boardService.updateBoard(boardId, updatedRequest);
    }

    @DeleteMapping("/boards/{boardId}")
    @Operation(summary = "Удаление доски")
    public void deleteBoard(
            @PathVariable Long boardId
    ) {
        boardService.deleteBoard(boardId);
    }

    @GetMapping("/boards/company/{companyId}")
    @Operation(summary = "Получить список досок компании")
    public List<BoardDto> getBoards(
            @PathVariable Long companyId
    ) {
        return queryService.getBoards(companyId);
    }

    @GetMapping("/boards/{boardId}")
    @Operation(summary = "Получить доску")
    public BoardDto getBoard(
            @PathVariable Long boardId
    ) {
        return queryService.getBoard(boardId);
    }

    // =========================================================
    // Управление столбцами
    // =========================================================

    @PostMapping("/boards/{boardId}/columns")
    @Operation(summary = "Создание столбца на доске")
    public void createColumn(
            @PathVariable Long boardId,
            @RequestBody CreateColumnRequest request
    ) {

        CreateColumnRequest updatedRequest =
                new CreateColumnRequest(
                        boardId,
                        request.name()
                );

        boardService.createColumn(updatedRequest);
    }

    @PutMapping("/boards/{boardId}/columns/{columnId}")
    @Operation(summary = "Изменение столбца")
    public void updateColumn(
            @PathVariable Long columnId,
            @RequestBody UpdateColumnRequest request
    ) {

        UpdateColumnRequest updatedRequest =
                new UpdateColumnRequest(
                        columnId,
                        request.name()
                );

        boardService.updateColumn(updatedRequest);
    }

    @PatchMapping("/boards/{boardId}/columns/{columnId}/move")
    @Operation(summary = "Изменение положения столбца на доске")
    public void moveColumn(
            @PathVariable Long boardId,
            @PathVariable Long columnId,
            @RequestBody MoveColumnRequest request
    ) {

        MoveColumnRequest updatedRequest =
                new MoveColumnRequest(
                        boardId,
                        columnId,
                        request.newIndex()
                );

        boardService.moveColumn(updatedRequest);
    }

    @DeleteMapping("/boards/{boardId}/columns/{columnId}")
    @Operation(summary = "Удаление столбца")
    public void deleteColumn(
            @PathVariable Long columnId
    ) {
        boardService.deleteColumn(columnId);
    }
}
