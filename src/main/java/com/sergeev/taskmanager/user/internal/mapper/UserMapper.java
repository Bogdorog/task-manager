package com.sergeev.taskmanager.user.internal.mapper;

import com.sergeev.taskmanager.media.api.MediaApi;
import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.internal.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Autowired
    protected MediaApi mediaApi;

    @Mapping(target = "login", source = "login")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "role", source = "role", ignore = true)
    public abstract User toEntity(UserDto dto);

    @Mapping(target = "role", expression = "java(user.getRole().getName())")
    @Mapping(target = "login", source = "login")
    @Mapping(target = "avatarUrl", expression = """
            java(user.getAvatarMediaId() != null
                            ? mediaApi.buildDownloadUrl(user.getAvatarMediaId())
                            : null)""")
    public abstract UserDto toResponse(User user);
}
