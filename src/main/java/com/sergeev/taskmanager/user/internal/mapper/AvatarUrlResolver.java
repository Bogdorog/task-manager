package com.sergeev.taskmanager.user.internal.mapper;

import com.sergeev.taskmanager.media.api.MediaApi;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AvatarUrlResolver {

    private final MediaApi mediaApi;

    @Named("resolveAvatarUrl")
    public String resolveAvatarUrl(UUID avatarMediaId) {
        return avatarMediaId != null
                ? mediaApi.buildDownloadUrl(avatarMediaId)
                : null;
    }
}
