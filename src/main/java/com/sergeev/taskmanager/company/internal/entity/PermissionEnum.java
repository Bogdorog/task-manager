package com.sergeev.taskmanager.company.internal.entity;

public enum PermissionEnum {
    CREATE_TASK("CREATE_TASK"),
    UPDATE_TASK("UPDATE_TASK"),
    DELETE_TASK("DELETE_TASK"),
    ASSIGN_TASK("ASSIGN_TASK"),
    VIEW_TASK("VIEW_TASK"),
    VIEW_ALL_TASKS("VIEW_ALL_TASKS"),
    MANAGE_COMPANY("MANAGE_COMPANY"),
    INVITE_USER("INVITE_USER");

    private String title;

    PermissionEnum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
