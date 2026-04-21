package com.sergeev.taskmanager.media.api.dto;

import java.util.UUID;

public record StorageSaveResult(
        UUID id,
        String checksum,
        String filePath
) {}
