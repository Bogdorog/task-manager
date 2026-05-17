package com.sergeev.taskmanager.task.internal.entity;

import lombok.Getter;

@Getter
public enum TaskPriority {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    CRITICAL("CRITICAL");
    private final String title;

    TaskPriority(String title) {
        this.title = title;
    }

}
