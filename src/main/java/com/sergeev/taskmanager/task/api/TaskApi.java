package com.sergeev.taskmanager.task.api;

import com.sergeev.taskmanager.task.api.dto.TaskCommentDto;
import com.sergeev.taskmanager.task.api.dto.TaskDto;
import com.sergeev.taskmanager.task.api.dto.request.*;

public interface TaskApi {

    TaskDto createTask(CreateTaskRequest request);

    TaskDto updateTask(UpdateTaskRequest request);

    void deleteTask(DeleteTaskRequest request);

    TaskCommentDto addComment(AddCommentRequest request);

    void deleteComment(DeleteCommentRequest request);
}
