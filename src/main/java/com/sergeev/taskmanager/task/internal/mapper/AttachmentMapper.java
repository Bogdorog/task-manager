package com.sergeev.taskmanager.task.internal.mapper;

import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;
import com.sergeev.taskmanager.task.api.dto.AttachmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachmentMapper {


    public AttachmentDto toDto(Long id, MediaAssetDto media) {
        return new AttachmentDto(id, media);
    }
}
