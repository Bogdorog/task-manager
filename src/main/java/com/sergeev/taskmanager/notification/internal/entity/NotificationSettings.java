package com.sergeev.taskmanager.notification.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "notification_settings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificationSettings {

    @Id
    private Long userId;

    @Column(nullable = false)
    @Builder.Default
    private Integer retentionDays = 30;
}