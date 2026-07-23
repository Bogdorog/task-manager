package com.sergeev.taskmanager.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на отправку письма")
public record SendEmailRequest(
        @Email(message = "Некорректный формат email")
        @NotBlank(message = "Email не может быть пустым")
        @Schema(description = "Адрес электронной почты получателя", example = "newuser@study.com")
        String to,
        @NotBlank(message = "Тема письма не может быть пустой")
        @Schema(description = "Тема письма", example = "Обновление политики использования")
        String subject,
        @NotBlank(message = "Текст письма не может быть пустым")
        @Schema(description = "Текст письма", example = "Html код письма")
        String text
) {}
