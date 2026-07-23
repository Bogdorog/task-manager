package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.notification.api.dto.NotificationEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseEmitterRegistry {

    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 минут
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        emitters.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
        }

        log.debug("SSE-подписка создана для пользователя {}", userId);
        return emitter;
    }

    public void send(Long userId, NotificationEventDto event) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            log.debug("Нет активных SSE-подписок для пользователя {}, событие пропущено", userId);
            return;
        }
        userEmitters.forEach(emitter -> sendSafely(emitter, event));
    }

    private void sendSafely(SseEmitter emitter, Object data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) return;
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

    // keep-alive сообщения
    @Scheduled(fixedRate = 20_000)
    public void heartbeat() {
        emitters.forEach((userId, list) ->
                list.forEach(emitter -> sendSafely(emitter, "ping")));
    }
}