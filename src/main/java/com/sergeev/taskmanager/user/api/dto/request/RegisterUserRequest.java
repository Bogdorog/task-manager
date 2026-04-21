package com.sergeev.taskmanager.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record RegisterUserRequest(
        @NotBlank(message = "Логин не может быть пустым")
        @Schema(description = "Логин пользователя", example = "user123")
        String login,

        @NotBlank(message = "ФИО не может быть пустым")
        @Schema(description = "Полное имя пользователя", example = "Сидоров Иван Сергеевич")
        String fullName,

        @NotBlank(message = "Телефон не может быть пустым")
        @Schema(description = "Телефон пользователя", example = "898712356")
        String phone,

        @Email(message = "Некорректный формат email")
        @NotBlank(message = "Email не может быть пустым")
        @Schema(description = "Адрес электронной почты пользователя", example = "newuser@study.com")
        String email,

        @NotBlank(message = "Адрес не может быть пустым")
        @Schema(description = "Адрес пользователя", example = "Россия, г. Самара, ул. Солнечная 29")
        String address,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
        @Schema(description = "Пароль пользователя", example = "secure_pass_123")
        String password
) {}