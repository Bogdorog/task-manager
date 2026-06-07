package com.sergeev.taskmanager.company.api.dto;

public record CompanyPermissionsDto(
        boolean canCreateTasks,
        boolean canUpdateTasks,
        boolean canDeleteTasks,
        boolean canAssignTasks,
        boolean canViewTasks, // В будущем нужно удалить
        boolean canViewAllTasks,
        boolean canInviteUsers,
        boolean canViewMembers,
        boolean canManageMembers,
        boolean canViewRoles,
        boolean canManageRoles,
        boolean canManageCompany,
        boolean canManageBoards
) {}
