package com.sergeev.taskmanager.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Смена пароля")
public record PasswordResetConfirmRequest(
        @NotBlank()
        @Schema(description = "Токен пользователя")
        String token,

        @NotBlank(message = "Пароль не может быть пустым")
        @Schema(description = "Пароль пользователя", example = "password123")
        String newPassword
) {}
