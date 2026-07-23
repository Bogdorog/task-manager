package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.notification.api.NotificationApi;
import com.sergeev.taskmanager.notification.api.NotificationType;
import com.sergeev.taskmanager.task.api.event.TaskDeadlineApproachingEvent;
import com.sergeev.taskmanager.task.api.event.TaskDeadlineOverdueEvent;
import com.sergeev.taskmanager.task.api.event.TaskDeletedEvent;
import com.sergeev.taskmanager.task.api.event.TaskMovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TaskNotificationListener {

    private final NotificationApi notificationApi;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskMoved(TaskMovedEvent event) {
        for (Long recipientId : recipientsExcludingActor(event.actorId(), event.creatorId(), event.assigneeId())) {
            notificationApi.notifyUser(recipientId, NotificationType.TASK_MOVED.toString(), Map.of(
                    "taskId", event.taskId(),
                    "taskTitle", event.taskTitle(),
                    "boardId", event.boardId(),
                    "fromColumn", event.fromColumnName(),
                    "toColumn", event.toColumnName()
            ));
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskDeleted(TaskDeletedEvent event) {
        for (Long recipientId : recipientsExcludingActor(event.actorId(), event.creatorId(), event.assigneeId())) {
            notificationApi.notifyUser(recipientId, NotificationType.TASK_DELETED.toString(), Map.of(
                    "taskId", event.taskId(),
                    "taskTitle", event.taskTitle(),
                    "boardId", event.boardId()
            ));
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeadlineApproaching(TaskDeadlineApproachingEvent event) {
        for (Long recipientId : recipients(event.creatorId(), event.assigneeId())) {
            notificationApi.notifyUser(recipientId, NotificationType.TASK_DEADLINE_APPROACHING.toString(), Map.of(
                    "taskId", event.taskId(),
                    "taskTitle", event.taskTitle(),
                    "boardId", event.boardId(),
                    "dueDate", event.dueDate()
            ));
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeadlineOverdue(TaskDeadlineOverdueEvent event) {
        for (Long recipientId : recipients(event.creatorId(), event.assigneeId())) {
            notificationApi.notifyUser(recipientId, NotificationType.TASK_DEADLINE_OVERDUE.toString(), Map.of(
                    "taskId", event.taskId(),
                    "taskTitle", event.taskTitle(),
                    "boardId", event.boardId(),
                    "dueDate", event.dueDate()
            ));
        }
    }

    /**
     * Метод определения кому отправлять уведомление. Actor исключается, чтобы человек не получал уведомление о своём же действии.
     * @param actorId Id пользователя, совершившего действие над задачей
     * @param creatorId Id создателя задачи
     * @param assigneeId Id ответственного за задачу
     * @return Список пользователей, которым нужно отправить уведомление
     */
    private Set<Long> recipientsExcludingActor(Long actorId, Long creatorId, Long assigneeId) {
        Set<Long> recipients = new HashSet<>();
        recipients.add(creatorId);
        if (assigneeId != null) {
            recipients.add(assigneeId);
        }
        recipients.remove(actorId);
        return recipients;
    }

    /**
     * Метод определения кому отправлять уведомление без actor'а.
     * @param creatorId Id создателя задачи
     * @param assigneeId Id ответственного за задачу
     * @return Список пользователей, которым нужно отправить уведомление
     */
    private Set<Long> recipients(Long creatorId, Long assigneeId) {
        Set<Long> recipients = new HashSet<>();
        recipients.add(creatorId);
        if (assigneeId != null) {
            recipients.add(assigneeId);
        }
        return recipients;
    }
}