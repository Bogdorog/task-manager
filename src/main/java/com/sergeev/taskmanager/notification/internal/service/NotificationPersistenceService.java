package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.notification.internal.entity.Notification;
import com.sergeev.taskmanager.notification.internal.entity.NotificationSettings;
import com.sergeev.taskmanager.notification.internal.mapper.NotificationMapper;
import com.sergeev.taskmanager.notification.internal.repository.NotificationRepository;
import com.sergeev.taskmanager.notification.internal.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationPersistenceService {

    public static final int MIN_RETENTION_DAYS = 1;
    public static final int MAX_RETENTION_DAYS = 365;
    public static final int DEFAULT_RETENTION_DAYS = 30;

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository settingsRepository;
    private final NotificationMapper mapper;

    @Transactional
    public Notification save(Long userId, String type, Object payload) {
        int retentionDays = settingsRepository.findById(userId)
                .map(NotificationSettings::getRetentionDays)
                .orElse(DEFAULT_RETENTION_DAYS);

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .payload(mapper.serializePayload(payload))
                .expiresAt(LocalDateTime.now().plusDays(retentionDays))
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public void updateRetention(Long userId, int days) {
        if (days < MIN_RETENTION_DAYS || days > MAX_RETENTION_DAYS) {
            throw new IllegalArgumentException(
                    "Срок хранения должен быть от %d до %d дней".formatted(MIN_RETENTION_DAYS, MAX_RETENTION_DAYS));
        }

        settingsRepository.save(
                NotificationSettings.builder().userId(userId).retentionDays(days).build());

        // Один из возможных подходов
        // Изменять срок хранения уже созданных уведомлений
        //return notificationRepository.rescheduleExpiryForUser(userId, days);
    }
}