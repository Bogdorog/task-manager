package com.sergeev.taskmanager.notification.internal.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergeev.taskmanager.notification.api.dto.NotificationEventDto;
import com.sergeev.taskmanager.notification.internal.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final ObjectMapper objectMapper;

    public String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Не удалось сериализовать payload уведомления: " + payload, e);
        }
    }

    public NotificationEventDto toDto(Notification notification) {
        return new NotificationEventDto(
                notification.getId(),
                notification.getType(),
                deserializePayload(notification.getPayload()),
                notification.getCreatedAt(),
                notification.getReadAt() != null
        );
    }

    private Object deserializePayload(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, Object.class);
        } catch (JsonProcessingException e) {
            // повреждённая запись в БД не должна ронять всю выдачу уведомлений
            throw new IllegalStateException(
                    "Повреждённый payload уведомления в БД, id=" + rawPayload, e);
        }
    }
}