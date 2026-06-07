package com.sergeev.taskmanager.task.internal.repository;

import com.sergeev.taskmanager.task.internal.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findAllByBoardIdOrderByPositionAsc(Long boardId);
    @Query("SELECT MAX(bc.position) FROM BoardColumn bc WHERE bc.board.id = :boardId")
    Integer findMaxPositionByBoard_Id(Long boardId);
}
