package com.sergeev.taskmanager.user.internal.mapper;

import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.UserShortDto;
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
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto dto);

    @Mapping(target = "role", expression = "java(user.getRole().getName())")
    @Mapping(target = "login", source = "login")
    @Mapping(target = "avatarUrl", source = "avatarMediaId", qualifiedByName = "resolveAvatarUrl")
    UserDto toDto(User user);

    @Mapping(target = "role", expression = "java(user.getRole().getName())")
    UserShortDto toShortDto(User user);

    default UserShortDto fromId(Long id) {
        if (id == null) return null;
        // Заполняем только id. Остальные поля будут null
        return new UserShortDto(id, null, null, null);
    }

    default Long toId(UserShortDto dto) {
        return dto != null ? dto.id() : null;
    }
}
