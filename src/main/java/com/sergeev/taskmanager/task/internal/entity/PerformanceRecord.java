package com.sergeev.taskmanager.task.internal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private Integer tasksCompletedCount;

    private Double averageCompletionTime;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;
}
