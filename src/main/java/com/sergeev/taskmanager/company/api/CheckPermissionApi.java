package com.sergeev.taskmanager.company.api;

import com.sergeev.taskmanager.company.api.dto.CompanyMembershipDto;

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

    CompanyMembershipDto getMembership(
            Long userId,
            Long companyId
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
            Long taskId
    );
}
