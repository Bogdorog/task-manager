package com.sergeev.taskmanager.task.api;

import com.sergeev.taskmanager.task.internal.entity.Task;
import com.sergeev.taskmanager.task.internal.entity.TaskHistoryField;

public interface TaskHistoryApi {
    void record(
            Task task,
            Long actorId,
            TaskHistoryField field,
            String oldValue,
            String newValue
    );
}
