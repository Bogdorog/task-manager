package com.sergeev.taskmanager.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Стандартизированный ответ, используемый при ошибках API")
public record ErrorResponse(
        @Schema(description = "Метка времени возникновения ошибки", example = "2025-10-29T11:00:00.000")
        LocalDateTime timestamp,

        @Schema(description = "HTTP-статус ошибки", example = "400")
        int status,

        @Schema(description = "Краткое название ошибки HTTP-статуса", example = "Bad Request")
        String error,

        @Schema(description = "Детальное сообщение об ошибке", example = "Поле 'title' не может быть пустым.")
        String message,

        @Schema(description = "Путь, на котором произошла ошибка", example = "/api/courses")
        String path
) {}