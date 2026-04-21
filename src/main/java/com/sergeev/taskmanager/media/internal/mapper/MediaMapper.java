package com.sergeev.taskmanager.media.internal.mapper;

import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;
import com.sergeev.taskmanager.media.internal.entity.MediaAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    MediaAsset toEntity(MediaAssetDto dto);
    @Mapping(target = "downloadUrl", source = "downloadUrl")
    MediaAssetDto toResponse(MediaAsset entity, String downloadUrl);
    MediaAssetDto toResponse(MediaAsset entity);
}
