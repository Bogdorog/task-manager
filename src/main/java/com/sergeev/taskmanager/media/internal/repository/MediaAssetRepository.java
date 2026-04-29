package com.sergeev.taskmanager.media.internal.repository;

import com.sergeev.taskmanager.media.internal.entity.MediaAsset;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    @NonNull Optional<MediaAsset> findById(UUID id);
}

