package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.notification.api.NotificationApi;
import com.sergeev.taskmanager.notification.internal.entity.Notification;
import com.sergeev.taskmanager.notification.internal.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationApiImpl implements NotificationApi {

    private final NotificationPersistenceService persistenceService;
    private final SseEmitterRegistry emitterRegistry;
    private final NotificationMapper mapper;

    @Override
    public void notifyUser(Long userId, String type, Object payload) {
        Notification saved = persistenceService.save(userId, type, payload);
        emitterRegistry.send(userId, mapper.toDto(saved));
    }
}