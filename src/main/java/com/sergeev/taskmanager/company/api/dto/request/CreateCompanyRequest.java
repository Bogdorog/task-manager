package com.sergeev.taskmanager.company.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос для создания компании")
public record CreateCompanyRequest(
        @NotBlank(message = "Название не может быть пустым")
        @Schema(description = "Название компании", example = "Компания ")
        String name,
        @Schema(description = "Описание компании", example = "Мы компания по производству компаний.")
        String description,
        @Schema(description = "Электронная почта компании", example = "company@mail.ru")
        @Email
        String email,
        @Schema(description = "Контактный телефон", example = "89879879897")
        String phone,
        @Schema(description = "Адрес компании", example = "Россия, г. Самара ул. Пушкина д. 109")
        String address
) {}
