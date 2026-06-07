package com.sergeev.taskmanager.task.internal.controller;

import com.sergeev.taskmanager.task.api.dto.AttachmentDto;
import com.sergeev.taskmanager.task.internal.service.TaskAttachmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachments")
public class AttachmentController {

    private final TaskAttachmentService attachmentService;

    /**
     * Загрузить вложение в задачу
     */
    @PostMapping(
            value = "/tasks/{taskId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public AttachmentDto uploadTaskAttachment(
            @PathVariable Long taskId,
            @RequestPart("file") MultipartFile file
    ) {

        return attachmentService.uploadTaskAttachment(
                taskId,
                file
        );
    }

    /**
     * Получить вложения задачи
     */
    @GetMapping("/tasks/{taskId}")
    public List<AttachmentDto> getTaskAttachments(
            @PathVariable Long taskId
    ) {

        return attachmentService.getTaskAttachments(
                taskId
        );
    }

    /**
     * Удалить вложение задачи
     */
    @DeleteMapping("/tasks/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskAttachment(
            @PathVariable Long attachmentId
    ) {

        attachmentService.deleteTaskAttachment(
                attachmentId
        );
    }

    /**
     * Загрузить вложение в комментарий
     */
    @PostMapping(
            value = "/comments/{commentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public AttachmentDto uploadCommentAttachment(
            @PathVariable Long commentId,
            @RequestPart("file") MultipartFile file
    ) {

        return attachmentService.uploadCommentAttachment(
                commentId,
                file
        );
    }

    /**
     * Получить вложения комментария
     */
    @GetMapping("/comments/{commentId}")
    public List<AttachmentDto> getCommentAttachments(
            @PathVariable Long commentId
    ) {

        return attachmentService.getCommentAttachments(
                commentId
        );
    }

    /**
     * Удалить вложение комментария
     */
    @DeleteMapping(
            "/comments/{commentId}/{mediaAssetId}"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentAttachment(
            @PathVariable Long commentId,
            @PathVariable UUID mediaAssetId
    ) {

        attachmentService.deleteCommentAttachment(
                commentId,
                mediaAssetId
        );
    }
}