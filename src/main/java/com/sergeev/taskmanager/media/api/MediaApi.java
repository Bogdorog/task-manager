package com.sergeev.taskmanager.media.api;

import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MediaApi {
    CompletableFuture<MediaAssetDto> upload(MultipartFile file, Long ownerId, Long incidentId) throws Exception;
    MediaAssetDto getMeta(UUID id);
    InputStream download(UUID id) throws Exception;
    void delete(UUID mediaAssetId, Long incidentId) throws Exception;
    void deleteCompletely(UUID mediaAssetId);
    void unlinkAvatar(UUID mediaAssetId, Long ownerId);
    String buildDownloadUrl(UUID id);
    Long getSize(MediaAssetDto a);
}

