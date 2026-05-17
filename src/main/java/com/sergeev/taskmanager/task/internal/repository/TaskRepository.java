package com.sergeev.taskmanager.task.internal.repository;

import com.sergeev.taskmanager.task.internal.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByBoardColumnId(Long columnId);

    List<Task> findAllByCompanyId(Long companyId);

    List<Task> findAllByCompanyIdAndAssignedTo(Long companyId, Long assignedTo);

    List<Task> findAllByCompanyIdAndCreatedBy(Long companyId, Long createdBy);

    List<Task> findAllByColumnId(Long columnId);

    List<Task> findAllByBoardId(Long boardId);
}
