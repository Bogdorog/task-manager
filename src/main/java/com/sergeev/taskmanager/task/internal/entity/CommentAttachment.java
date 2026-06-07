package com.sergeev.taskmanager.task.internal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "comment_attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CommentAttachmentId.class)
public class CommentAttachment {
    @Id
    @Column(name = "comment_id")
    private Long commentId;

    @Id
    @Column(name = "media_asset_id", nullable = false)
    private UUID mediaAssetId;
}
