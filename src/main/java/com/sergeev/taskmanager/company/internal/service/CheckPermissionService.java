package com.sergeev.taskmanager.company.internal.service;

import com.sergeev.taskmanager.company.api.CheckPermissionApi;
import com.sergeev.taskmanager.company.internal.entity.Company;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.company.internal.mapper.CompanyMembershipMapper;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.task.api.dto.TaskDto;
import com.sergeev.taskmanager.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckPermissionService implements CheckPermissionApi {
    private final CompanyMembershipRepository membershipRepository;
    private final CompanyMembershipMapper membershipMapper;
    private final UserApi userApi;

    @Override
    public void checkCompanyPermission(
            Long userId,
            Long companyId,
            String permission) {

        if (!hasCompanyPermission(userId, companyId, permission)
                && !isCompanyOwner(userId, companyId)) {
            throw new AccessDeniedException("Недостаточно прав");
        }
    }

    public void checkCanViewTask(Long userId, TaskDto task) {

        if (!hasCompanyPermission(userId, task.companyId(),
                PermissionEnum.VIEW_ALL_TASKS.getTitle())
                && !userId.equals(task.createdBy().id())
                && !userId.equals(task.assignedTo().id())
                && !isCompanyOwner(userId, task.companyId()))
        {
            throw new AccessDeniedException("Недостаточно прав");
        }
    }

    public boolean checkCanViewTasks(Long userId, Long companyId) {

        if (!hasCompanyPermission(userId, companyId,
                PermissionEnum.VIEW_ALL_TASKS.getTitle())
                && !isCompanyOwner(userId, companyId))
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasCompanyPermission(
            Long userId,
            Long companyId,
            String permission) {

        return membershipRepository
                .findByUserIdAndCompanyId(
                        userId,
                        companyId
                )
                .map(membership ->
                        membership.getRole()
                                .hasPermission(PermissionEnum.valueOf(permission))
                )
                .orElse(false);
    }

    @Override
    public boolean isCompanyMember(Long userId, Long companyId) {

        return membershipRepository
                .existsByUserIdAndCompanyId(userId, companyId);
    }

    @Override
    public boolean isCompanyOwner(
            Long userId,
            Long companyId) {

        return membershipRepository
                .findByUserIdAndCompanyId(userId, companyId)
                .map(m ->
                        Company.OWNER.equals(m.getRole().getName()))
                .orElse(false);
    }
}
