package com.sergeev.taskmanager.media.internal.service;

import com.sergeev.taskmanager.media.api.FileStorage;
import com.sergeev.taskmanager.media.api.MediaApi;
import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;
import com.sergeev.taskmanager.media.api.dto.StorageSaveResult;
import com.sergeev.taskmanager.media.internal.entity.MediaAsset;
import com.sergeev.taskmanager.media.internal.mapper.MediaMapper;
import com.sergeev.taskmanager.media.internal.repository.MediaAssetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class MediaService implements MediaApi {

    private final FileStorage fileStorage;
    private final MediaAssetRepository mediaRepo;
    private final String downloadBase;
    private final MediaMapper mediaMapper;

    public MediaService(
            @Qualifier("resilientFileStorage") FileStorage fileStorage,
            MediaAssetRepository mediaRepo,
            @Value("${app.media.download-base:/api/media}") String downloadBase,
            MediaMapper mediaMapper
    ) {
        this.fileStorage = fileStorage;
        this.mediaRepo = mediaRepo;
        this.downloadBase = downloadBase;
        this.mediaMapper = mediaMapper;
    }

    @Transactional
    public MediaAssetDto upload(MultipartFile file, Long ownerId) {
        try {
            StorageSaveResult result = fileStorage.save(file).get();

            MediaAsset asset = mediaRepo.findById(result.id()).orElseGet(() -> {
                MediaAsset newAsset = new MediaAsset();
                newAsset.setId(result.id());
                newAsset.setOriginalName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
                newAsset.setStoredName(result.filePath());
                newAsset.setMimeType(file.getContentType());
                newAsset.setFileSize(file.getSize());
                newAsset.setUploadedBy(ownerId);
                return mediaRepo.save(newAsset);
            });

            return mediaMapper.toDto(asset, buildDownloadUrl(asset.getId()));
        } catch (Exception e) {
            log.error("Ошибка при загрузке файла", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось загрузить файл", e);
        }
    }

    public MediaAssetDto getMeta(UUID id) {
        MediaAsset asset = mediaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Медиафайл не найден"));
        return mediaMapper.toDto(asset, buildDownloadUrl(id));
    }

    public Long getSize(UUID id) {
        MediaAsset asset = mediaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Медиафайл не найден"));
        return asset.getFileSize();
    }

    public InputStream download(UUID id) throws Exception {
        MediaAsset asset = mediaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Медиафайл не найден"));

        Path path = Paths.get(asset.getStoredName()).normalize();
        return fileStorage.load(path.toString()).get();
    }

    @Transactional
    public void delete(UUID id) {
        MediaAsset asset = mediaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Медиафайл не найден"));

        mediaRepo.delete(asset);

        try {
            fileStorage.delete(asset.getStoredName())
                    .exceptionally(ex -> {
                        log.warn("Не удалось удалить файл с диска: {}", asset.getStoredName(), ex);
                        return false;
                    });
        } catch (Exception e) {
            log.error("Ошибка при удалении файла: {}", e.getMessage());
        }
    }

    public String buildDownloadUrl(UUID id) {
        return downloadBase + "/" + id + "/download";
    }
}
