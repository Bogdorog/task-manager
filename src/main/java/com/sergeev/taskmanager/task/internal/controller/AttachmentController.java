package com.sergeev.taskmanager.task.internal.controller;

import com.sergeev.taskmanager.task.api.dto.AttachmentDto;
import com.sergeev.taskmanager.task.internal.service.TaskAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping(value = "/tasks/{taskId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавить вложение к задаче")
    public AttachmentDto uploadTaskAttachment(
            @PathVariable Long taskId,
            @RequestPart("file") MultipartFile file) {
        return attachmentService.uploadTaskAttachment(
                taskId,
                file
        );
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Получить вложения задачи")
    public List<AttachmentDto> getTaskAttachments(@PathVariable Long taskId) {
        return attachmentService.getTaskAttachments(
                taskId
        );
    }

    @DeleteMapping("/tasks/{attachmentId}")
    @Operation(summary = "Удалить вложение задачи")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskAttachment(@PathVariable Long attachmentId) {
        attachmentService.deleteTaskAttachment(
                attachmentId
        );
    }

    @PostMapping(value = "/comments/{commentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавить вложение к комментарию")
    public AttachmentDto uploadCommentAttachment(
            @PathVariable Long commentId,
            @RequestPart("file") MultipartFile file) {
        return attachmentService.uploadCommentAttachment(
                commentId,
                file
        );
    }

    @GetMapping("/comments/{commentId}")
    @Operation(summary = "Получить вложения комментария")
    public List<AttachmentDto> getCommentAttachments(
            @PathVariable Long commentId
    ) {
        return attachmentService.getCommentAttachments(
                commentId
        );
    }

    @DeleteMapping("/comments/{commentId}/{mediaAssetId}")
    @Operation(summary = "Удалить вложение комментария")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentAttachment(
            @PathVariable Long commentId,
            @PathVariable UUID mediaAssetId) {
        attachmentService.deleteCommentAttachment(
                commentId,
                mediaAssetId
        );
    }
}