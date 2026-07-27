package com.sergeev.taskmanager.notification.api;

public interface NotificationApi {
    void notifyUser(Long userId, Long companyId, String type, Object payload);
}