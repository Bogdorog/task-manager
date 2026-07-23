package com.sergeev.taskmanager.notification.internal.repository;

import com.sergeev.taskmanager.notification.internal.entity.NotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {
}
