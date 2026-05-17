package com.sergeev.taskmanager.task.internal.service;

import com.sergeev.taskmanager.task.api.TaskHistoryApi;
import com.sergeev.taskmanager.task.internal.entity.Task;
import com.sergeev.taskmanager.task.internal.entity.TaskHistory;
import com.sergeev.taskmanager.task.internal.entity.TaskHistoryField;
import com.sergeev.taskmanager.task.internal.repository.TaskHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskHistoryService
        implements TaskHistoryApi {

    private final TaskHistoryRepository historyRepository;

    @Override
    public void record(
            Task task,
            Long actorId,
            TaskHistoryField field,
            String oldValue,
            String newValue
    ) {

        validateArguments(task, actorId, field);

        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        TaskHistory history = TaskHistory.builder()
                .taskId(task.getId())
                .changedBy(actorId)
                .fieldName(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }

    private void validateArguments(
            Task task,
            Long actorId,
            TaskHistoryField field
    ) {

        if (task == null) {
            throw new IllegalArgumentException(
                    "Task cannot be null"
            );
        }

        if (task.getId() == null) {
            throw new IllegalArgumentException(
                    "Task id cannot be null"
            );
        }

        if (actorId == null) {
            throw new IllegalArgumentException(
                    "Actor id cannot be null"
            );
        }

        if (field == null) {
            throw new IllegalArgumentException(
                    "History field cannot be null"
            );
        }
    }
}
