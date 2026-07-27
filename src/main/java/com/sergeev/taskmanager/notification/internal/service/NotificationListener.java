package com.sergeev.taskmanager.notification.internal.service;

import com.sergeev.taskmanager.company.api.CheckPermissionApi;
import com.sergeev.taskmanager.company.api.PermissionEnum;
import com.sergeev.taskmanager.company.api.dto.event.MemberAddedEvent;
import com.sergeev.taskmanager.company.api.dto.event.RoleAddedEvent;
import com.sergeev.taskmanager.notification.api.NotificationApi;
import com.sergeev.taskmanager.notification.api.NotificationType;
import com.sergeev.taskmanager.task.api.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationApi notificationApi;
    private final CheckPermissionApi permissionService;

    // ===========================================
    // СОБЫТИЯ ЗАДАЧ
    // ===========================================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskMoved(TaskMovedEvent event) {
        for (Long recipientId : recipientsExcludingActor(event.actorId(), event.creatorId(), event.assigneeId())) {
            notificationApi.notifyUser(recipientId, event.companyId(), NotificationType.TASK_MOVED.toString(), Map.of(
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
            notificationApi.notifyUser(recipientId, event.companyId(), NotificationType.TASK_DELETED.toString(), Map.of(
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
            notificationApi.notifyUser(recipientId, event.companyId(), NotificationType.TASK_DEADLINE_APPROACHING.toString(), Map.of(
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
            notificationApi.notifyUser(recipientId, event.companyId(), NotificationType.TASK_DEADLINE_OVERDUE.toString(), Map.of(
                    "taskId", event.taskId(),
                    "taskTitle", event.taskTitle(),
                    "boardId", event.boardId(),
                    "dueDate", event.dueDate()
            ));
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskUpdated(TaskUpdatedEvent event) {
        for (Long recipientId : recipientsExcludingActor(event.actorId(), event.creatorId(), event.assigneeId())) {
            notificationApi.notifyUser(recipientId, event.companyId(), NotificationType.TASK_UPDATED.toString(), Map.of(
                    "taskId", event.taskId(),
                    "taskTitle", event.taskTitle(),
                    "boardId", event.boardId()
            ));
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskAssigned(TaskAssignedEvent event) {
        if (event.newAssigneeId() == null || event.newAssigneeId().equals(event.actorId())) {
            return; // не уведомляем, если сняли назначение или назначили самому себе
        }

        notificationApi.notifyUser(event.newAssigneeId(), event.companyId(), NotificationType.TASK_ASSIGNED.toString(), Map.of(
                "taskId", event.taskId(),
                "taskTitle", event.taskTitle(),
                "boardId", event.boardId()
        ));
    }

    // ===========================================
    // СОБЫТИЯ КОМПАНИЙ
    // ===========================================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberAdded(MemberAddedEvent event) {
        List<Long> recipients = permissionService.findUserIdsWithPermission(
                event.companyId(), PermissionEnum.MANAGE_MEMBERS);

        for (Long recipientId : recipients) {
            if (recipientId.equals(event.actorId())) continue; // не уведомляем того, кто сам это сделал

            notificationApi.notifyUser(recipientId, event.companyId(), NotificationType.MEMBER_ADDED.toString(), Map.of(
                    "companyId", event.companyId(),
                    "newMemberName", event.newMemberName(),
                    "roleName", event.roleName(),
                    "actorName", event.actorName()
            ));
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoleAdded(RoleAddedEvent event) {
        List<Long> recipients = permissionService.findUserIdsWithPermission(
                event.companyId(), PermissionEnum.MANAGE_ROLES);

        for (Long recipientId : recipients) {
            if (recipientId.equals(event.actorId())) continue;

            notificationApi.notifyUser(recipientId, event.companyId(), NotificationType.ROLE_ADDED.toString(), Map.of(
                    "companyId", event.companyId(),
                    "roleName", event.roleName(),
                    "actorName", event.actorName()
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