package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.notification.api.EmailNotificationApi;
import com.sergeev.taskmanager.notification.api.dto.request.SendEmailRequest;
import com.sergeev.taskmanager.user.api.event.PasswordResetRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountDeleteListener {
    private final EmailNotificationApi emailApi;
    private final EmailTemplateService emailTemplateService;

    @ApplicationModuleListener
    public void handle(PasswordResetRequestedEvent event) {

        String html = emailTemplateService.buildResetPasswordEmail(event.token());

        emailApi.sendEmail(
                new SendEmailRequest(
                        event.email(),
                        "Удаление аккаунта",
                        html
                )
        );
    }
}
