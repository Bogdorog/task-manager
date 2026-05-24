package com.sergeev.taskmanager.task.internal.service;

import com.sergeev.taskmanager.company.api.CheckPermissionApi;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import com.sergeev.taskmanager.task.api.dto.BoardColumnDto;
import com.sergeev.taskmanager.task.api.dto.BoardDto;
import com.sergeev.taskmanager.task.api.dto.request.*;
import com.sergeev.taskmanager.task.internal.entity.Board;
import com.sergeev.taskmanager.task.internal.entity.BoardColumn;
import com.sergeev.taskmanager.task.internal.entity.Task;
import com.sergeev.taskmanager.task.internal.mapper.BoardColumnMapper;
import com.sergeev.taskmanager.task.internal.mapper.BoardMapper;
import com.sergeev.taskmanager.task.internal.repository.BoardColumnRepository;
import com.sergeev.taskmanager.task.internal.repository.BoardRepository;
import com.sergeev.taskmanager.task.internal.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private static final int POSITION_STEP = 1000;
    private static final int PREALLOCATED_POSITIONS = 60;

    private final BoardRepository boardRepository;
    private final BoardColumnRepository columnRepository;
    private final TaskRepository taskRepository;
    private final CheckPermissionApi permissionApi;
    private final BoardMapper boardMapper;
    private final BoardColumnMapper columnMapper;
    private final SecurityFacadeApi securityFacade;

    // =========================================================
    // BOARD
    // =========================================================

    public BoardDto createBoard(CreateBoardRequest request) {
        permissionApi.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                request.companyId(),
                PermissionEnum.MANAGE_BOARDS.name()
        );

        Board board = Board.builder()
                .companyId(request.companyId())
                .name(request.name())
                .description(request.description())
                .createdAt(LocalDateTime.now())
                .build();

        boardRepository.save(board);

        createDefaultColumns(board);

        return boardMapper.toDto(board);
    }

    public BoardDto updateBoard(Long boardId, UpdateBoardRequest request) {
        Board board = getBoard(boardId);

        permissionApi.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                board.getCompanyId(),
                PermissionEnum.MANAGE_BOARDS.name()
        );

        board.setName(request.name());
        board.setDescription(request.description());

        return boardMapper.toDto(board);
    }

    public void deleteBoard(
            Long boardId) {

        Board board = getBoard(boardId);

        permissionApi.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                board.getCompanyId(),
                PermissionEnum.MANAGE_BOARDS.name()
        );

        boardRepository.delete(board);
    }

    // =========================================================
    // COLUMN
    // =========================================================

    public BoardColumnDto createColumn(CreateColumnRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        Board board = getBoard(request.boardId());

        permissionApi.checkCompanyPermission(
                actorId,
                board.getCompanyId(),
                PermissionEnum.MANAGE_BOARDS.name()
        );

        Integer position = generateNextPosition(
                columnRepository.findMaxPositionByBoardId(
                        board.getId()
                )
        );

        BoardColumn column = BoardColumn.builder()
                .board(board)
                .name(request.name())
                .position(position)
                .createdAt(LocalDateTime.now())
                .build();

        columnRepository.save(column);

        return columnMapper.toDto(column);
    }

    public BoardColumnDto updateColumn(Long columnId, String name) {
        BoardColumn column = getColumn(columnId);

        permissionApi.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                column.getBoard().getCompanyId(),
                PermissionEnum.MANAGE_BOARDS.name()
        );

        column.setName(name);

        return columnMapper.toDto(column);
    }

    public void moveColumn(MoveColumnRequest request) {
        BoardColumn column = getColumn(request.columnId());

        permissionApi.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                column.getBoard().getCompanyId(),
                PermissionEnum.MANAGE_BOARDS.name()
        );

        List<BoardColumn> columns =
                columnRepository.findAllByBoardIdOrderByPositionAsc(
                        column.getBoard().getId()
                );

        validateTargetPosition(
                request.newIndex(),
                columns.size()
        );

        columns.removeIf(c ->
                Objects.equals(c.getId(), column.getId())
        );

        columns.add(request.newIndex(), column);

        recalculateColumnPositions(columns);
    }

    public void deleteColumn(Long columnId) {

        BoardColumn column = getColumn(columnId);

        permissionApi.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                column.getBoard().getCompanyId(),
                PermissionEnum.MANAGE_BOARDS.name()
        );

        boolean hasTasks =
                taskRepository.existsByColumnId(
                        column.getId()
                );

        if (hasTasks) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Нельзя удалить колонку с задачами"
            );
        }

        Board board = column.getBoard();

        columnRepository.delete(column);

        reorganizePositionsIfNeeded(board.getId());
    }

    // =========================================================
    // MOVE TASK BETWEEN COLUMNS
    // =========================================================

    public void moveTask(MoveTaskRequest request) {
        Task task = taskRepository.findById(
                request.taskId()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Задача не найдена"
                )
        );

        BoardColumn targetColumn = getColumn(request.targetColumnId());

        validateSameBoard(
                getColumn(task.getColumnId()),
                targetColumn
        );

        permissionApi.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                taskRepository.findCompanyIdByTaskId(request.taskId()),
                PermissionEnum.UPDATE_TASK.name()
        );

        task.setColumnId(targetColumn.getId());
        task.setUpdatedAt(LocalDateTime.now());
    }

    // =========================================================
    // PRIVATE
    // =========================================================

    private Board getBoard(Long boardId) {

        return boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Доска не найдена"
                        )
                );
    }

    private BoardColumn getColumn(Long columnId) {

        return columnRepository.findById(columnId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Колонка не найдена"
                        )
                );
    }

    private void createDefaultColumns(Board board) {

        List<String> defaultColumns = List.of(
                "Backlog",
                "In Progress",
                "Done"
        );

        List<BoardColumn> columns = new ArrayList<>();

        int position = 0;

        for (String name : defaultColumns) {

            columns.add(
                    BoardColumn.builder()
                            .board(board)
                            .name(name)
                            .position(position)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            position += POSITION_STEP;
        }

        columnRepository.saveAll(columns);
    }

    /**
     * Алгоритм:
     * До 60 колонок:
     * 0
     * 1000
     * 2000
     * ...
     * После:
     * max + 1
     */
    private Integer generateNextPosition(
            Integer maxPosition
    ) {

        if (maxPosition == null) {
            return 0;
        }

        int nextPreallocated =
                maxPosition + POSITION_STEP;

        if (nextPreallocated
                <= PREALLOCATED_POSITIONS * POSITION_STEP) {

            return nextPreallocated;
        }

        return maxPosition + 1;
    }

    /**
     * Полный перерасчет позиций.
     * Используется:
     * - при moveColumn
     * - при reorganize
     */
    private void recalculateColumnPositions(
            List<BoardColumn> columns
    ) {

        int position = 0;

        for (BoardColumn column : columns) {

            column.setPosition(position);

            position += POSITION_STEP;
        }
    }

    /**
     * Реорганизация нужна только когда:
     * количество колонок <= 60
     * Тогда возвращаем:
     * 0
     * 1000
     * 2000
     * ...
     */
    private void reorganizePositionsIfNeeded(
            Long boardId
    ) {

        List<BoardColumn> columns =
                columnRepository.findAllByBoardIdOrderByPositionAsc(
                        boardId
                );

        if (columns.size() > PREALLOCATED_POSITIONS) {
            return;
        }

        recalculateColumnPositions(columns);
    }

    private void validateTargetPosition(
            Integer target,
            Integer size
    ) {

        if (target < 0 || target >= size) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Некорректная позиция"
            );
        }
    }

    private void validateSameBoard(
            BoardColumn source,
            BoardColumn target
    ) {

        if (!Objects.equals(
                source.getBoard().getId(),
                target.getBoard().getId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Колонки принадлежат разным доскам"
            );
        }
    }
}
