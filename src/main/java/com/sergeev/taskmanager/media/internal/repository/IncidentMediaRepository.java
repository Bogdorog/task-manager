package com.sergeev.taskmanager.media.internal.repository;

import com.sergeev.taskmanager.media.internal.entity.IncidentMedia;
import com.sergeev.taskmanager.media.internal.entity.MediaAsset;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentMediaRepository  extends CrudRepository<IncidentMedia, Long> {

    // Найти инцидент, к которому привязан файл (проверить)
    Optional<IncidentMedia> findByMediaAssetIdAndIncidentId(UUID mediaAssetId, Long incidentId);

    // Найти все файлы для инцидента
    @Query("SELECT im.mediaAsset FROM IncidentMedia im WHERE im.incidentId = :incidentId")
    List<MediaAsset> findMediaAssetsByIncidentId(Long incidentId);

    Long countByMediaAssetId(UUID mediaAssetId);

    void deleteAllByMediaAssetId(UUID mediaAssetId);

    Optional<IncidentMedia> findByMediaAssetIdAndIncidentIdNull(UUID id, Long ownerId);
}
