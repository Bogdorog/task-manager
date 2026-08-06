package com.sergeev.taskmanager.notification.api;
//TODO Добавить новые виды уведомлений
public enum NotificationType {
    TASK_CREATED("TASK_CREATED"),
    TASK_UPDATED("TASK_UPDATED"),
    TASK_MOVED("TASK_MOVED"),
    TASK_DELETED("TASK_DELETED"),
    TASK_ASSIGNED("TASK_ASSIGNED"),
    TASK_DEADLINE_APPROACHING("TASK_DEADLINE_APPROACHING"),
    TASK_DEADLINE_OVERDUE("TASK_DEADLINE_OVERDUE"),
    MEMBER_ADDED("MEMBER_ADDED"),
    MEMBER_ROLE_CHANGED("MEMBER_ROLE_CHANGED"),
    MEMBER_DELETED("MEMBER_DELETED"),
    MEMBER_LEFT("MEMBER_LEFT"),
    ROLE_ADDED("ROLE_ADDED"),
    ROLE_CHANGED("ROLE_CHANGED"),
    ROLE_DELETED("ROLE_DELETED");

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
