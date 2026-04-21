package com.sergeev.taskmanager.media.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO для представления медиа-ресурса")
public record MediaAssetDto(
        @Schema(description = "Уникальный идентификатор медиа-ресурса", example = "12332323")
        UUID id,

        @Schema(description = "Идентификатор владельца ресурса", example = "789")
        Long ownerId,

        @Schema(description = "Идентификатор инцидента, к которому прикреплен ресурс", example = "123")
        Long incidentId,

        @Schema(description = "Имя файла", example = "incident-image.jpg")
        String fileName,

        @Schema(description = "URL для скачивания файла", example = "/api/v1/assets/12332323/download")
        String downloadUrl
) {}