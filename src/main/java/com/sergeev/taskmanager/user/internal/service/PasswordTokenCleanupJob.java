package com.sergeev.taskmanager.user.internal.service;

import com.sergeev.taskmanager.user.internal.repository.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
class PasswordTokenCleanupJob {

    private final PasswordResetTokenRepository repository;

    PasswordTokenCleanupJob(PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *") // каждый час
    void cleanup() {
        repository.deleteExpiredOrUsed(LocalDateTime.now());
    }
}
