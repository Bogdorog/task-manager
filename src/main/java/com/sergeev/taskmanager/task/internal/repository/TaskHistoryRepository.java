package com.sergeev.taskmanager.task.internal.repository;

import com.sergeev.taskmanager.task.internal.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findAllByTaskIdOrderByChangedAtDesc(Long taskId);
}
