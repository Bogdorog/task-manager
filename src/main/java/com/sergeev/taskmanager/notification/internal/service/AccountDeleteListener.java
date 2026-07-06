package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.notification.api.EmailNotificationApi;
import com.sergeev.taskmanager.notification.api.dto.request.SendEmailRequest;
import com.sergeev.taskmanager.user.api.event.AccountDeletionRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountDeleteListener {
    private final EmailNotificationApi emailApi;
    private final EmailTemplateService emailTemplateService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @ApplicationModuleListener
    public void handle(AccountDeletionRequestedEvent event) {

        String link = frontendUrl + "/account-delete?token=" + event.token();

        String html = emailTemplateService.buildAccountDeletionEmail(link);

        emailApi.sendEmail(
                new SendEmailRequest(
                        event.email(),
                        "Удаление аккаунта",
                        html
                )
        );
    }
}
