package com.sergeev.taskmanager.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос для аутентификации пользователя")
public record LoginRequest(
        @NotBlank(message = "Логин не может быть пустым")
        @Schema(description = "Логин пользователя", example = "user123")
        String login,

        @NotBlank(message = "Пароль не может быть пустым")
        @Schema(description = "Пароль пользователя", example = "password123")
        String password
) {}
