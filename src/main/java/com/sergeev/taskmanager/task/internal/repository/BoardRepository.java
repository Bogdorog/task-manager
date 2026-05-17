package com.sergeev.taskmanager.task.internal.repository;

import com.sergeev.taskmanager.task.internal.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findAllByCompanyId(Long companyId);
}
