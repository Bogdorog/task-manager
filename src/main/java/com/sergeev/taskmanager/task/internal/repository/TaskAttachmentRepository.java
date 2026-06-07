package com.sergeev.taskmanager.task.internal.repository;

import com.sergeev.taskmanager.task.internal.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findAllByTaskId(Long taskId);

    long countByMediaAssetId(UUID mediaAssetId);

    void deleteByMediaAssetId(UUID mediaAssetId);
}
