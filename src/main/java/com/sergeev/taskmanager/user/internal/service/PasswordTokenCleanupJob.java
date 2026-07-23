package com.sergeev.taskmanager.user.internal.service;

import com.sergeev.taskmanager.user.internal.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
class PasswordTokenCleanupJob {

    private final PasswordResetTokenRepository repository;

    @Scheduled(cron = "0 0 1 * * *") // каждый день в 1:00
    @Transactional(rollbackFor = Exception.class)
    void cleanup() {
        int deleted = repository.deleteExpiredOrUsed(LocalDateTime.now());
        log.info("Очистка уведомлений: удалено {} истёкших записей", deleted);
    }
}
