package com.sergeev.taskmanager.task.internal.service;

import com.sergeev.taskmanager.company.api.CheckPermissionApi;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.media.api.MediaApi;
import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;
import com.sergeev.taskmanager.security.internal.utils.SecurityFacade;
import com.sergeev.taskmanager.task.api.dto.AttachmentDto;
import com.sergeev.taskmanager.task.internal.entity.CommentAttachment;
import com.sergeev.taskmanager.task.internal.entity.CommentAttachmentId;
import com.sergeev.taskmanager.task.internal.entity.TaskAttachment;
import com.sergeev.taskmanager.task.internal.entity.TaskComment;
import com.sergeev.taskmanager.task.internal.mapper.AttachmentMapper;
import com.sergeev.taskmanager.task.internal.repository.CommentAttachmentRepository;
import com.sergeev.taskmanager.task.internal.repository.TaskAttachmentRepository;
import com.sergeev.taskmanager.task.internal.repository.TaskCommentRepository;
import com.sergeev.taskmanager.task.internal.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskAttachmentService {

    private final MediaApi mediaApi;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final CommentAttachmentRepository commentAttachmentRepository;
    private final CheckPermissionApi permissionApi;
    private final SecurityFacade securityFacade;
    private final AttachmentMapper attachmentMapper;

    /**
     * Загрузить вложение в задачу
     */
    public AttachmentDto uploadTaskAttachment(
            Long taskId,
            MultipartFile file
    ) {

        Long actorId = securityFacade.getCurrentUserId();

        MediaAssetDto media =
                mediaApi.upload(file, actorId);

        TaskAttachment attachment =
                TaskAttachment.builder()
                        .taskId(taskId)
                        .uploadedBy(actorId)
                        .mediaAssetId(media.id())
                        .build();

        taskAttachmentRepository.save(attachment);

        return attachmentMapper.toDto(attachment.getId(), media);
    }

    /**
     * Получить вложения задачи
     */
    @Transactional(readOnly = true)
    public List<AttachmentDto> getTaskAttachments(
            Long taskId
    ) {
        List<TaskAttachment> attachments =
                taskAttachmentRepository.findAllByTaskId(taskId);

        return attachments.stream()
                .map(attachment -> {
                    MediaAssetDto media = mediaApi.getMeta(attachment.getMediaAssetId());
                    return new AttachmentDto(
                            attachment.getId(), media);
                })
                .toList();
    }

    /**
     * Удалить вложение задачи
     */
    public void deleteTaskAttachment(
            Long attachmentId
    ) {

        Long actorId = securityFacade.getCurrentUserId();

        TaskAttachment attachment =
                taskAttachmentRepository.findById(attachmentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Вложение не найдено"
                                ));

        permissionApi.checkCompanyPermission(
                actorId,
                taskRepository.findCompanyIdByTaskId(attachment.getTaskId()),
                PermissionEnum.UPDATE_TASK.name()
        );

        UUID mediaId = attachment.getMediaAssetId();

        taskAttachmentRepository.delete(attachment);

        deleteMediaIfUnused(mediaId);
    }

    /**
     * Загрузить вложение в комментарий
     */
    public AttachmentDto uploadCommentAttachment(
            Long commentId,
            MultipartFile file
    ) {

        Long actorId = securityFacade.getCurrentUserId();

        MediaAssetDto media =
                mediaApi.upload(file, actorId);

        CommentAttachment attachment =
                CommentAttachment.builder()
                        .commentId(commentId)
                        .mediaAssetId(media.id())
                        .build();

        commentAttachmentRepository.save(
                attachment
        );

        return attachmentMapper.toDto(commentId, media);
    }

    /**
     * Получить вложения комментария
     */
    @Transactional(readOnly = true)
    public List<AttachmentDto> getCommentAttachments(
            Long commentId
    ) {

        List<CommentAttachment> attachments =
                commentAttachmentRepository
                        .findAllByCommentId(commentId);

        return attachments.stream()
                .map(attachment -> {
                    MediaAssetDto media = mediaApi.getMeta(attachment.getMediaAssetId());
                    return new AttachmentDto(
                            attachment.getCommentId(), media);
                })
                .toList();
    }

    /**
     * Удалить вложение комментария
     */
    public void deleteCommentAttachment(
            Long commentId,
            UUID mediaAssetId
    ) {

        Long actorId = securityFacade.getCurrentUserId();

        TaskComment comment =
                commentRepository.findById(commentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Комментарий не найден"
                                ));

        permissionApi.checkCompanyPermission(
                actorId,
                taskRepository.findCompanyIdByTaskId(comment.getTaskId()),
                PermissionEnum.UPDATE_TASK.name()
        );

        CommentAttachmentId id =
                new CommentAttachmentId(
                        commentId,
                        mediaAssetId
                );

        CommentAttachment attachment =
                commentAttachmentRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Вложение не найдено"
                                ));

        commentAttachmentRepository.delete(
                attachment
        );

        deleteMediaIfUnused(mediaAssetId);
    }

    /**
     * Удаление файла,
     * если на него больше никто не ссылается
     */
    private void deleteMediaIfUnused(
            UUID mediaAssetId
    ) {

        long taskRefs =
                taskAttachmentRepository
                        .countByMediaAssetId(mediaAssetId);

        long commentRefs =
                commentAttachmentRepository
                        .countByMediaAssetId(mediaAssetId);

        if (taskRefs + commentRefs == 0) {

            mediaApi.delete(mediaAssetId);
        }
    }
}
