package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.notification.api.EmailNotificationApi;
import com.sergeev.taskmanager.notification.api.dto.request.SendEmailRequest;
import com.sergeev.taskmanager.user.api.event.PasswordResetRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetListener {

    private final EmailNotificationApi emailApi;
    private final EmailTemplateService emailTemplateService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @ApplicationModuleListener
    public void handle(PasswordResetRequestedEvent event) {

        String link = frontendUrl + "/reset-password?token=" + event.token();

        String html = emailTemplateService.buildResetPasswordEmail(link);

        emailApi.sendEmail(
                new SendEmailRequest(
                        event.email(),
                        "Сброс пароля",
                        html
                )
        );
    }
}
