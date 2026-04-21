package com.sergeev.taskmanager.notification.api;

import com.sergeev.taskmanager.notification.api.dto.request.SendEmailRequest;
import com.sergeev.taskmanager.notification.api.dto.request.SendEmailWithAttachmentRequest;

public interface EmailNotificationApi {

    void sendEmail(SendEmailRequest request);

    void sendEmailWithAttachment(SendEmailWithAttachmentRequest request);

}
