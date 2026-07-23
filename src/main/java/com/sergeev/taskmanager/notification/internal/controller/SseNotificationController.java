package com.sergeev.taskmanager.notification.internal.controller;

import com.sergeev.taskmanager.notification.api.dto.NotificationEventDto;
import com.sergeev.taskmanager.notification.api.dto.request.UpdateRetentionRequest;
import com.sergeev.taskmanager.notification.internal.entity.Notification;
import com.sergeev.taskmanager.notification.internal.mapper.NotificationMapper;
import com.sergeev.taskmanager.notification.internal.repository.NotificationRepository;
import com.sergeev.taskmanager.notification.internal.service.NotificationPersistenceService;
import com.sergeev.taskmanager.notification.internal.service.SseEmitterRegistry;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "SSE", description = "Рассылка уведомлений")
public class SseNotificationController {

    private final SseEmitterRegistry emitterRegistry;
    private final NotificationRepository notificationRepository;
    private final NotificationPersistenceService persistenceService;
    private final NotificationMapper mapper;
    private final SecurityFacadeApi securityFacade;

    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Подписаться на рассылку уведомлений")
    public SseEmitter subscribe() {
        if (securityFacade.getCurrentUserId() == null) {
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new AccessDeniedException("Не аутентифицирован"));
            return emitter;
        }

        try {
            return emitterRegistry.createEmitter(securityFacade.getCurrentUserId());
        } catch (Exception e) {
            log.error("Не удалось создать SSE-подписку для userId={}", securityFacade.getCurrentUserId(), e);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(e);
            return emitter;
        }
    }

    @GetMapping
    @Operation(summary = "Получить список уведомлений")
    public List<NotificationEventDto> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByUserIdAndReadAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                securityFacade.getCurrentUserId(), now)
                : notificationRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
                securityFacade.getCurrentUserId(), now, PageRequest.of(0, 100));

        return notifications.stream().map(mapper::toDto).toList();
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Отметить уведомление как прочитанное")
    @Transactional
    public void markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Уведомление не найдено"));

        if (!notification.getUserId().equals(securityFacade.getCurrentUserId())) {
            throw new AccessDeniedException("Нет доступа к этому уведомлению");
        }

        notification.setReadAt(LocalDateTime.now());
    }

    @PutMapping("/settings/retention")
    @Operation(summary = "Изменить срок хранения уведомлений")
    public void updateRetention(@RequestBody @Valid UpdateRetentionRequest request) {
        persistenceService.updateRetention(securityFacade.getCurrentUserId(), request.days());
    }
}