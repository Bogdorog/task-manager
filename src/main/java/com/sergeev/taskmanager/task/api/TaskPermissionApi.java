package com.sergeev.taskmanager.task.api;

import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.task.internal.entity.Task;

public interface TaskPermissionApi {
    void checkCompanyPermission(
            Long userId,
            Long companyId,
            PermissionEnum permission
    );

    void checkCanViewTask(Long userId, Task task);

    void checkCanEditTask(Long userId, Task task);

    CompanyMembership getMembership(Long userId, Long companyId);
}
