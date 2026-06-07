package com.sergeev.taskmanager.task.internal.repository;

import com.sergeev.taskmanager.task.internal.entity.CommentAttachment;
import com.sergeev.taskmanager.task.internal.entity.CommentAttachmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentAttachmentRepository extends JpaRepository<CommentAttachment, CommentAttachmentId> {
    List<CommentAttachment> findAllByCommentId(Long commentId);

    long countByMediaAssetId(UUID mediaAssetId);
}
