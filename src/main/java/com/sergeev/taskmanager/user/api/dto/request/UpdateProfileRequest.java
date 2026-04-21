package com.sergeev.taskmanager.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на изменение профиля пользователя")
public record UpdateProfileRequest(
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
        String address
) {}
