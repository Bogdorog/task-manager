package com.sergeev.taskmanager.media.internal.service;

import com.sergeev.taskmanager.media.api.FileStorage;
import com.sergeev.taskmanager.media.api.MediaApi;
import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;
import com.sergeev.taskmanager.media.internal.entity.IncidentMedia;
import com.sergeev.taskmanager.media.internal.entity.MediaAsset;
import com.sergeev.taskmanager.media.internal.mapper.MediaMapper;
import com.sergeev.taskmanager.media.internal.repository.IncidentMediaRepository;
import com.sergeev.taskmanager.media.internal.repository.MediaAssetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MediaService implements MediaApi{

    private final FileStorage fileStorage;
    private final MediaAssetRepository mediaRepo;
    private final String downloadBase;
    private final MediaMapper mediaMapper;
    private final TransactionTemplate tx;
    private final Path storageRoot;
    private final IncidentMediaRepository incidentMediaRepo;

    public MediaService(
            @Qualifier("resilientFileStorage") FileStorage fileStorage,
            MediaAssetRepository mediaRepo,
            @Value("${app.media.download-base:/api/media}") String downloadBase,
            MediaMapper mediaMapper,
            TransactionTemplate tx,
            FileSystemStorage fileSystemStorage,
            IncidentMediaRepository incidentMediaRepo
    ) {
        this.fileStorage   = fileStorage;
        this.mediaRepo     = mediaRepo;
        this.downloadBase  = downloadBase;
        this.mediaMapper   = mediaMapper;
        this.tx            = tx;
        this.storageRoot   = fileSystemStorage.getStorageRoot();
        this.incidentMediaRepo = incidentMediaRepo;
    }

    @Override
    public CompletableFuture<MediaAssetDto> upload(MultipartFile file, Long ownerId, Long incidentId) throws Exception {
        return fileStorage.save(file)
                .thenApply(result -> tx.execute(status -> {

                    return mediaRepo.findById(result.id())
                            .map(existing -> {
                                if (incidentId == null) {
                                    // Личное фото/аватар: проверяем, нет ли уже связи с NULL
                                    if (incidentMediaRepo.findByMediaAssetIdAndIncidentIdNull(existing.getId(), ownerId).isEmpty()) {
                                        IncidentMedia link = new IncidentMedia();
                                        link.setIncidentId(null);
                                        link.setMediaAsset(existing);
                                        link.setUserId(ownerId);
                                        incidentMediaRepo.save(link);
                                    }
                                } else {
                                    // Файл уже есть в хранилище — просто создаём новую привязку к инциденту
                                    if (incidentMediaRepo.findByMediaAssetIdAndIncidentId(existing.getId(), incidentId).isEmpty()) {
                                        IncidentMedia link = new IncidentMedia();
                                        link.setIncidentId(incidentId);
                                        link.setMediaAsset(existing);
                                        link.setUserId(ownerId);
                                        incidentMediaRepo.save(link);
                                    }
                                }
                                return mediaMapper.toResponse(existing, buildDownloadUrl(existing.getId()));
                            })
                            .orElseGet(() -> {
                                // Новый файл — создаём сущность и привязку
                                MediaAsset asset = new MediaAsset();
                                asset.setId(result.id());
                                asset.setFileName(file.getOriginalFilename());
                                asset.setFilePath(result.filePath());
                                mediaRepo.save(asset);

                                IncidentMedia link = new IncidentMedia();
                                link.setIncidentId(incidentId);
                                link.setMediaAsset(asset);
                                link.setUserId(ownerId);
                                incidentMediaRepo.save(link);

                                return mediaMapper.toResponse(asset, buildDownloadUrl(asset.getId()));
                            });
                }));
    }

    @Override
    public MediaAssetDto getMeta(UUID id) {
        MediaAsset a = mediaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Медиафайл не найден"));
        return mediaMapper.toResponse(a, buildDownloadUrl(id));
    }

    @Override
    public Long getSize(MediaAssetDto a) {
        MediaAsset asset = mediaRepo.findById(a.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Медиафайл не найден"));

        Path path = storageRoot.resolve(asset.getFilePath()).normalize();
        if (!Files.exists(path))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл отсутствует на диске");
        return path.toFile().length();
    }

    @Override
    public InputStream download(UUID id) throws IOException {
        MediaAsset a = mediaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Медиафайл не найден"));

        Path path = storageRoot.resolve(a.getFilePath()).normalize();
        if (!Files.exists(path))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл отсутствует на диске");

        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    @Transactional
    protected void unlinkFromIncident(UUID mediaAssetId, Long incidentId) {
        IncidentMedia link = incidentMediaRepo
                .findByMediaAssetIdAndIncidentId(mediaAssetId, incidentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Связь файла с инцидентом не найдена"
                ));

        incidentMediaRepo.delete(link);


    }

    @Transactional
    public void unlinkAvatar(UUID mediaAssetId, Long ownerId) {
        Optional<IncidentMedia> personalLink = incidentMediaRepo
                .findByMediaAssetIdAndIncidentIdNull(mediaAssetId, ownerId);

        if (personalLink.isPresent()) {
            incidentMediaRepo.delete(personalLink.get());

            long remainingLinks = incidentMediaRepo.countByMediaAssetId(mediaAssetId);

            if (remainingLinks == 0) {
                deleteCompletely(mediaAssetId);
            }
        }
    }

    @Transactional
    public void deleteCompletely(UUID mediaAssetId) {
        MediaAsset asset = mediaRepo.findById(mediaAssetId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Медиафайл не найден"
                ));

        // Удаляем все связи с инцидентами
        incidentMediaRepo.deleteAllByMediaAssetId(mediaAssetId);
        mediaRepo.delete(asset);
        try {
            fileStorage.delete(asset.getFilePath())
                    .exceptionally(ex -> {
                        log.warn("Не удалось удалить файл с диска: {}", asset.getFilePath(), ex);
                        return false;
                    });
        } catch (Exception e) {
            log.error("Ошибка при удалении файла: {}", e.getMessage());
        }
    }


    @Transactional
    @Override
    public void delete(UUID mediaAssetId, Long incidentId) {
        // Проверка, что файл действительно привязан к этому инциденту
        boolean linked = incidentMediaRepo
                .findByMediaAssetIdAndIncidentId(mediaAssetId, incidentId)
                .isPresent();

        if (!linked) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фото не найдено в этом инциденте");
        }

        long remainingLinks = incidentMediaRepo.countByMediaAssetId(mediaAssetId) - 1;
        unlinkFromIncident(mediaAssetId, incidentId);

        // Если связанный инцидентов больше не осталось — удаляем файл полностью
        if (remainingLinks <= 0) {
            MediaAsset asset = mediaRepo.findById(mediaAssetId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден"));

            mediaRepo.delete(asset);
            try {
                fileStorage.delete(asset.getFilePath())
                        .exceptionally(ex -> {
                            log.warn("Не удалось удалить файл с диска: {}", asset.getFilePath(), ex);
                            return false;
                        });
            } catch (Exception e) {
                log.error("Ошибка при удалении файла: {}", e.getMessage());
            }

        }
    }

    public String buildDownloadUrl(UUID id) {
        return downloadBase + "/" + id + "/download";
    }
}
