package com.sergeev.taskmanager.task.internal.repository;

import com.sergeev.taskmanager.task.internal.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findAllByTaskId(Long taskId);
}
