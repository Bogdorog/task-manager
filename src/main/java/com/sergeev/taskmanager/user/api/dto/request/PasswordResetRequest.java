package com.sergeev.taskmanager.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на смену пароля")
public record PasswordResetRequest(
        @Email(message = "Некорректный формат email")
        @NotBlank(message = "Email не может быть пустым")
        @Schema(description = "Адрес электронной почты пользователя", example = "user@mail.com")
        String email
) {}
