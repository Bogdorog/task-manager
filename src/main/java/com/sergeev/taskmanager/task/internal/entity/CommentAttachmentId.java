package com.sergeev.taskmanager.task.internal.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CommentAttachmentId {
    private Long commentId;
    private UUID mediaAssetId;
}
