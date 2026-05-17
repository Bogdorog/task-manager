package com.sergeev.taskmanager.task.internal.entity;

import lombok.Getter;

@Getter
public enum TaskStatus {
    OPEN("OPEN"),
    IN_PROGRESS("IN_PROGRESS"),
    ON_REVIEW("ON_REVIEW"),
    DONE("DONE"),
    CANCELLED("CANCELLED");

    private final String title;

    TaskStatus(String title) {
        this.title = title;
    }

}