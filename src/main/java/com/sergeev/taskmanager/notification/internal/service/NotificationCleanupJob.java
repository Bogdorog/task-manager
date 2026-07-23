package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.notification.internal.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupJob {

    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 30 1 * * *") // каждый день в 1:30
    @Transactional
    public void cleanupExpired() {
        int deleted = notificationRepository.deleteAllExpired(LocalDateTime.now());
        log.info("Очистка уведомлений: удалено {} истёкших записей", deleted);
    }
}