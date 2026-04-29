package com.sergeev.taskmanager.user.internal.mapper;

import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.internal.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = AvatarUrlResolver.class)
public interface UserMapper {

    @Mapping(target = "login", source = "login")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "avatarMediaId", ignore = true)
    User toEntity(UserDto dto);

    @Mapping(target = "role", expression = "java(user.getRole().getName())")
    @Mapping(target = "login", source = "login")
    @Mapping(target = "avatarUrl", source = "avatarMediaId", qualifiedByName = "resolveAvatarUrl")
    UserDto toResponse(User user);
}
