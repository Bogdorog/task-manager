package com.sergeev.taskmanager.company.internal.entity;

import lombok.Getter;

@Getter
public enum PermissionEnum {
    CREATE_TASK("CREATE_TASK"),
    UPDATE_TASK("UPDATE_TASK"),
    DELETE_TASK("DELETE_TASK"),
    ASSIGN_TASK("ASSIGN_TASK"),
    VIEW_TASK("VIEW_TASK"), // В будущем нужно удалить
    VIEW_ALL_TASKS("VIEW_ALL_TASKS"),
    MANAGE_COMPANY("MANAGE_COMPANY"),
    MANAGE_ROLES("MANAGE_ROLES"),
    INVITE_USER("INVITE_USER"),
    MANAGE_BOARDS("MANAGE_BOARDS"),
    // Это разрешение не должно появляться в бд,
    // но является внутренним условием на вызов особой проверки
    COMMENT_TASK("COMMENT_TASK"),
    DELETE_COMMENT("DELETE_COMMENT");

    private final String title;

    PermissionEnum(String title) {
        this.title = title;
    }

}
