package com.sergeev.taskmanager.notification.api;

public interface NotificationApi {
    void notifyUser(Long userId, String type, Object payload);
}