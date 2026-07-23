package com.sergeev.taskmanager.notification.internal.repository;

import com.sergeev.taskmanager.notification.internal.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime now, Pageable pageable);

    List<Notification> findByUserIdAndReadAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime now);

    @Modifying
    @Query("delete from Notification n where n.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);
}
