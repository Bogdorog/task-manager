package com.sergeev.taskmanager.task.internal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// TODO Посмотреть как и стоит ли переводить хранение с LocalDateTime на Instant
@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_task_assigned", columnList = "assigned_to"),
                @Index(name = "idx_tasks_column", columnList = "column_id"),
                @Index(name = "idx_tasks_created_by", columnList = "created_by"),
                @Index(name = "idx_tasks_due_date", columnList = "due_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "column_id", nullable = false)
    private Long columnId;

    @Column
    private LocalDateTime dueDate;

    @Column(name = "deadline_warning_sent", nullable = false)
    @Builder.Default
    private Boolean deadlineWarningSent = false;

    @Column(name = "deadline_overdue_sent", nullable = false)
    @Builder.Default
    private Boolean deadlineOverdueSent = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "taskId",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TaskComment> comments = new ArrayList<>();

    @OneToMany(
            mappedBy = "taskId",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TaskAttachment> attachments = new ArrayList<>();

    @OneToMany(
            mappedBy = "taskId",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TaskHistory> history = new ArrayList<>();
}