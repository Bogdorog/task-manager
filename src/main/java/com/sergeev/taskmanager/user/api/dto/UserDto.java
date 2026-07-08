package com.sergeev.taskmanager.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Полная модель данных пользователя")
public record UserDto(
        @Schema(description = "Уникальный идентификатор пользователя",
                example = "123")
        Long id,
        @Schema(description = "Логин пользователя",
                example = "sergeev")
        String login,
        @Schema(description = "Полное имя пользователя",
                example = "Сергеев Даниил Сергеевич")
        String fullName,
        @Schema(description = "Номер телефона пользователя",
                example = "8 987 987 87 87")
        String phone,
        @Schema(description = "Уникальный идентификатор пользователя",
                example = "123")
        String email,
        @Schema(description = "Электронная почта пользователя",
                example = "sergeev@gmail.com")
        String address,
        @Schema(description = "Фактический адрес пользователя",
                example = "Россия, Самарская область, г. Самара, ул. Самарская, д. 29, кв. 87")
        String role,
        @Schema(description = "Ссылка на аварар пользователя",
                example = "/api/media/11223344-11aa-2233-44bc-1a2b3c4d5e6f/download")
        String avatarUrl
) {}