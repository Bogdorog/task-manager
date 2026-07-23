package com.sergeev.taskmanager.notification.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Полная модель данных пользователя")
public record NotificationEventDto(
        @Schema(description = "Уникальный идентификатор пользователя",
                example = "123")
        Long id,
        @Schema(description = "Тип уведомления",
                example = "TASK_ASSIGNED")
        String type,
        @Schema(description = "Содержание уведомления")
        Object payload,
        @Schema(description = "Время создания уведомления")
        LocalDateTime createdAt,
        @Schema(description = "Статус уведомления")
        boolean read
) {}