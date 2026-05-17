package com.sergeev.taskmanager.task.internal.controller;

import com.sergeev.taskmanager.task.api.dto.BoardDto;
import com.sergeev.taskmanager.task.api.dto.TaskCommentDto;
import com.sergeev.taskmanager.task.api.dto.TaskDto;
import com.sergeev.taskmanager.task.api.dto.request.*;
import com.sergeev.taskmanager.task.internal.service.BoardService;
import com.sergeev.taskmanager.task.internal.service.TaskCommandService;
import com.sergeev.taskmanager.task.internal.service.TaskQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskCommandService commandService;
    private final TaskQueryService queryService;
    private final BoardService boardService;

    // =========================================================
    // TASK COMMANDS
    // =========================================================

    @PostMapping
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
                request.actorId(),
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate()
        );

        return commandService.updateTask(updatedRequest);
    }

    @PatchMapping("/{taskId}/assign")
    public TaskDto assignTask(
            @PathVariable Long taskId,
            @RequestBody AssignTaskRequest request
    ) {

        AssignTaskRequest updatedRequest =
                new AssignTaskRequest(
                        taskId,
                        request.actorId(),
                        request.assignedUserId()
                );

        return commandService.assignTask(updatedRequest);
    }

    @PatchMapping("/{taskId}/status")
    public TaskDto changeStatus(
            @PathVariable Long taskId,
            @RequestBody ChangeTaskStatusRequest request
    ) {

        ChangeTaskStatusRequest updatedRequest =
                new ChangeTaskStatusRequest(
                        taskId,
                        request.actorId(),
                        request.status()
                );

        return commandService.changeStatus(updatedRequest);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(
            @PathVariable Long taskId,
            @RequestBody DeleteTaskRequest request
    ) {

        DeleteTaskRequest updatedRequest =
                new DeleteTaskRequest(
                        taskId,
                        request.actorId()
                );

        commandService.deleteTask(updatedRequest);
    }

    // =========================================================
    // COMMENTS
    // =========================================================

    @PostMapping("/{taskId}/comments")
    public TaskCommentDto addComment(
            @PathVariable Long taskId,
            @RequestBody AddCommentRequest request
    ) {

        AddCommentRequest updatedRequest =
                new AddCommentRequest(
                        taskId,
                        request.actorId(),
                        request.text()
                );

        return commandService.addComment(updatedRequest);
    }

    @DeleteMapping("/{taskId}/comments/{commentId}")
    public void deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @RequestBody DeleteCommentRequest request
    ) {

        DeleteCommentRequest updatedRequest =
                new DeleteCommentRequest(
                        request.actorId(),
                        commentId
                );

        commandService.deleteComment(updatedRequest);
    }

    // =========================================================
    // TASK QUERIES
    // =========================================================

    @GetMapping("/{taskId}")
    public TaskDto getTask(
            @PathVariable Long taskId,
            @RequestParam Long actorId
    ) {
        return queryService.getTask(actorId, taskId);
    }

    @GetMapping("/company/{companyId}")
    public List<TaskDto> getCompanyTasks(
            @PathVariable Long companyId,
            @RequestParam Long actorId
    ) {
        return queryService.getCompanyTasks(
                actorId,
                companyId
        );
    }

    @GetMapping("/company/{companyId}/my")
    public List<TaskDto> getMyTasks(
            @PathVariable Long companyId,
            @RequestParam Long actorId
    ) {
        return queryService.getMyTasks(
                actorId,
                companyId
        );
    }

    @GetMapping("/company/{companyId}/created")
    public List<TaskDto> getCreatedTasks(
            @PathVariable Long companyId,
            @RequestParam Long actorId
    ) {
        return queryService.getCreatedTasks(
                actorId,
                companyId
        );
    }

    @GetMapping("/columns/{columnId}")
    public List<TaskDto> getColumnTasks(
            @PathVariable Long columnId,
            @RequestParam Long actorId
    ) {
        return queryService.getColumnTasks(
                actorId,
                columnId
        );
    }

    @GetMapping("/{taskId}/comments")
    public List<TaskCommentDto> getComments(
            @PathVariable Long taskId,
            @RequestParam Long actorId
    ) {
        return queryService.getTaskComments(
                actorId,
                taskId
        );
    }

    @GetMapping("/{taskId}/history")
    public List<?> getTaskHistory(
            @PathVariable Long taskId,
            @RequestParam Long actorId
    ) {
        return queryService.getTaskHistory(
                actorId,
                taskId
        );
    }

    // =========================================================
    // BOARDS
    // =========================================================

    @PostMapping("/boards")
    public BoardDto createBoard(
            @RequestBody CreateBoardRequest request
    ) {
        return boardService.createBoard(request);
    }

    @PutMapping("/boards/{boardId}")
    public BoardDto updateBoard(
            @PathVariable Long boardId,
            @RequestBody UpdateBoardRequest request
    ) {

        UpdateBoardRequest updatedRequest =
                new UpdateBoardRequest(
                        boardId,
                        request.actorId(),
                        request.name(),
                        request.description()
                );

        return boardService.updateBoard(boardId, updatedRequest);
    }

    @DeleteMapping("/boards/{boardId}")
    public void deleteBoard(
            @PathVariable Long boardId,
            @RequestParam Long actorId
    ) {
        boardService.deleteBoard(
                boardId,
                actorId
        );
    }

    @GetMapping("/boards/company/{companyId}")
    public List<BoardDto> getBoards(
            @PathVariable Long companyId,
            @RequestParam Long actorId
    ) {
        return queryService.getBoards(
                actorId,
                companyId
        );
    }

    @GetMapping("/boards/{boardId}")
    public BoardDto getBoard(
            @PathVariable Long boardId,
            @RequestParam Long actorId
    ) {
        return queryService.getBoard(
                actorId,
                boardId
        );
    }

    // =========================================================
    // COLUMNS
    // =========================================================

    @PostMapping("/boards/{boardId}/columns")
    public void createColumn(
            @PathVariable Long boardId,
            @RequestBody CreateColumnRequest request
    ) {

        CreateColumnRequest updatedRequest =
                new CreateColumnRequest(
                        boardId,
                        request.actorId(),
                        request.name()
                );

        boardService.createColumn(updatedRequest);
    }

    @PatchMapping("/boards/{boardId}/columns/{columnId}/move")
    public void moveColumn(
            @PathVariable Long boardId,
            @PathVariable Long columnId,
            @RequestBody MoveColumnRequest request
    ) {

        MoveColumnRequest updatedRequest =
                new MoveColumnRequest(
                        boardId,
                        columnId,
                        request.actorId(),
                        request.newIndex()
                );

        boardService.moveColumn(updatedRequest);
    }

    @DeleteMapping("/boards/{boardId}/columns/{columnId}")
    public void deleteColumn(
            @PathVariable Long boardId,
            @PathVariable Long columnId,
            @RequestParam Long actorId
    ) {
        boardService.deleteColumn(
                columnId,
                actorId
        );
    }

    // =========================================================
    // TASK MOVE
    // =========================================================

    @PatchMapping("/{taskId}/move")
    public void moveTask(
            @PathVariable Long taskId,
            @RequestBody MoveTaskRequest request
    ) {

        MoveTaskRequest updatedRequest =
                new MoveTaskRequest(
                        request.actorId(),
                        taskId,
                        request.targetColumnId(),
                        request.newPosition()
                );

        boardService.moveTask(updatedRequest);
    }
}
