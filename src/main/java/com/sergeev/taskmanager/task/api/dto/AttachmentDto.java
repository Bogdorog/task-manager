package com.sergeev.taskmanager.task.api.dto;

import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;

public record AttachmentDto(
        Long id,
        MediaAssetDto media
) {}
