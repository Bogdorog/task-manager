package com.sergeev.taskmanager.task.internal.service;

import com.sergeev.taskmanager.task.api.event.TaskDeadlineApproachingEvent;
import com.sergeev.taskmanager.task.api.event.TaskDeadlineOverdueEvent;
import com.sergeev.taskmanager.task.internal.entity.Task;
import com.sergeev.taskmanager.task.internal.entity.TaskStatus;
import com.sergeev.taskmanager.task.internal.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineNotificationJob {

    private static final List<TaskStatus> EXCLUDED_STATUSES = List.of(TaskStatus.DONE, TaskStatus.CANCELLED);

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.notifications.deadline-warning-hours:24}")
    private long warningWindowHours;

    // каждые 15 минут
    @Scheduled(fixedRate = 15 * 60 * 1000L)
    @Transactional
    public void checkDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        checkApproaching(now);
        checkOverdue(now);
    }

    private void checkApproaching(LocalDateTime now) {
        LocalDateTime threshold = now.plus(Duration.ofHours(warningWindowHours));

        List<Task> tasks = taskRepository.findApproachingDeadline(now, threshold, EXCLUDED_STATUSES);
        for (Task task : tasks) {
            eventPublisher.publishEvent(new TaskDeadlineApproachingEvent(
                    task.getId(), task.getTitle(), task.getBoardId(),
                    task.getDueDate(), task.getCreatedBy(),
                    task.getAssignedTo() != null ? task.getAssignedTo() : null
            ));
            task.setDeadlineWarningSent(true);
        }
        if (!tasks.isEmpty()) {
            log.info("Отправлено предупреждений о приближающемся дедлайне: {}", tasks.size());
        }
    }

    private void checkOverdue(LocalDateTime now) {
        List<Task> tasks = taskRepository.findOverdue(now, EXCLUDED_STATUSES);
        for (Task task : tasks) {
            eventPublisher.publishEvent(new TaskDeadlineOverdueEvent(
                    task.getId(), task.getTitle(), task.getBoardId(),
                    task.getDueDate(), task.getCreatedBy(),
                    task.getAssignedTo() != null ? task.getAssignedTo() : null
            ));
            task.setDeadlineOverdueSent(true);
        }
        if (!tasks.isEmpty()) {
            log.info("Отправлено уведомлений о просроченном дедлайне: {}", tasks.size());
        }
    }
}