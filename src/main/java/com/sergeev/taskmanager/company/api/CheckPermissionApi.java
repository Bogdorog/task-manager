package com.sergeev.taskmanager.company.api;

import com.sergeev.taskmanager.task.api.dto.TaskDto;

public interface CheckPermissionApi {

    void checkCompanyPermission(
            Long userId,
            Long companyId,
            String permission
    );

    boolean hasCompanyPermission(
            Long userId,
            Long companyId,
            String permission
    );

    boolean isCompanyMember(
            Long userId,
            Long companyId
    );

    boolean isCompanyOwner(
            Long userId,
            Long companyId
    );

    void checkCanViewTask(
            Long userId,
            TaskDto task
    );
}
