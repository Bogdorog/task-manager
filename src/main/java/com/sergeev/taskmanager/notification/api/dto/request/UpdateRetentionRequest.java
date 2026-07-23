package com.sergeev.taskmanager.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос для изменения срока хранения уведомлений")
public record UpdateRetentionRequest(
        @NotBlank(message = "Количество дней не может быть пустым")
        @Schema(description = "Срок хранения в днях", example = "30")
        @Min(1) @Max(365) int days
) {}
