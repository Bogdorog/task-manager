package com.sergeev.taskmanager.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Смена пароля в профиле")
public record ChangePasswordRequest(
        @NotBlank()
        @Schema(description = "Старый пароль пользователя", example = "password123")
        String oldPassword,

        @NotBlank(message = "Пароль не может быть пустым")
        @Schema(description = "Новый пароль пользователя", example = "password123")
        String newPassword
) {}