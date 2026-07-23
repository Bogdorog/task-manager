package com.sergeev.taskmanager.notification.api;

public enum NotificationType {
    TASK_CREATED("TASK_CREATED"),
    TASK_UPDATED("TASK_UPDATED"),
    TASK_MOVED("TASK_MOVED"),
    TASK_DELETED("TASK_DELETED"),
    TASK_ASSIGNED("TASK_ASSIGNED"),
    TASK_DEADLINE_APPROACHING("TASK_DEADLINE_APPROACHING"),
    TASK_DEADLINE_OVERDUE("TASK_DEADLINE_OVERDUE"),
    MEMBER_ADDED("MEMBER_ADDED"),
    ROLE_ADDED("ROLE_ADDED");

    private final String title;

    @Override
    public String toString()
    {
        return this.title;
    }

    NotificationType(String title) {
        this.title = title;
    }
}
