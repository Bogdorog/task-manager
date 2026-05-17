package com.sergeev.taskmanager.company.internal.service;

import com.sergeev.taskmanager.company.api.dto.CompanyMembershipDto;
import com.sergeev.taskmanager.company.api.dto.request.DeleteMemberRequest;
import com.sergeev.taskmanager.company.api.dto.request.InviteUserRequest;
import com.sergeev.taskmanager.company.api.dto.request.LeaveCompanyRequest;
import com.sergeev.taskmanager.company.internal.entity.Company;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import com.sergeev.taskmanager.company.internal.entity.CompanyRole;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.company.internal.mapper.CompanyMembershipMapper;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRoleRepository;
import com.sergeev.taskmanager.exception.BusinessRuleException;
import com.sergeev.taskmanager.user.api.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//TODO Переписать получение членства на метод из CheckPermissionService

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyMembershipService {
    private final CompanyMembershipRepository membershipRepository;
    private final CompanyRepository companyRepository;
    private final CompanyRoleRepository roleRepository;
    private final CheckPermissionService permissionService;
    private final UserApi userApi;
    private final CompanyMembershipMapper mapper;

    public void inviteUser(
            InviteUserRequest request
    ) throws BusinessRuleException {
        permissionService.checkCompanyPermission(
                request.inviterId(),
                request.companyId(),
                PermissionEnum.INVITE_USER.getTitle()
        );
        userApi.getUserById(request.userId());

        boolean alreadyMember =
                membershipRepository
                        .existsByUserIdAndCompanyId(
                                request.userId(),
                                request.companyId()
                        );

        if (alreadyMember) {

            throw new BusinessRuleException(
                    "Пользователь уже состоит в компании"
            );
        }

        CompanyRole role =
                roleRepository
                        .findByCompanyIdAndId(
                                request.companyId(),
                                request.roleId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Роль не найдена"
                                )
                        );

        if (Company.OWNER.equals(
                role.getName()
        )) {

            throw new BusinessRuleException(
                    "Нельзя назначить роль владельца"
            );
        }

        Company company =
                companyRepository
                        .findById(request.companyId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Компания не найдена"
                                )
                        );

        CompanyMembership membership =
                CompanyMembership.builder()
                        .userId(request.userId())
                        .company(company)
                        .role(role)
                        .joinedAt(LocalDateTime.now())
                        .build();

        membershipRepository.save(membership);
    }

    public void removeUser(
            DeleteMemberRequest request
    ) throws BusinessRuleException {
        permissionService.checkCompanyPermission(
                request.actorId(),
                request.companyId(),
                PermissionEnum.MANAGE_COMPANY.getTitle()
        );

        CompanyMembership membership =
                membershipRepository
                        .findById(request.membershipId())
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Сотрудник не найден"
                                )
                        );

        if (!membership.getCompany().getId()
                .equals(request.companyId())) {

            throw new BusinessRuleException(
                    "Сотрудник не принадлежит компании"
            );
        }

        if (permissionService.isCompanyOwner(membership.getUserId(), membership.getCompany().getId()))
        {
            throw new BusinessRuleException("Нельзя удалить Владельца");
        }
        if (membership.getUserId()
                .equals(request.actorId())) {
            throw new BusinessRuleException("Нельзя удалить самого себя");
        }
        membershipRepository.delete(membership);
    }

    public void leaveCompany(
            LeaveCompanyRequest request
    ) throws BusinessRuleException {
        CompanyMembership membership =
                membershipRepository
                        .findByUserIdAndCompanyId(
                                request.actorId(),
                                request.companyId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Пользователь не состоит в компании"
                                )
                        );

        if (permissionService.isCompanyOwner(membership.getUserId(), membership.getCompany().getId()))
        {
            throw new BusinessRuleException("Нельзя покинуть компанию будучи Владельцем");
        }

        membershipRepository.delete(membership);
    }

    @Transactional(readOnly = true)
    public List<CompanyMembershipDto> getMembers(
            Long companyId,
            Long actorId
    ) {
        if (permissionService.isCompanyMember(actorId, companyId))
        {
            return membershipRepository
                    .findAllByCompanyId(companyId)
                    .stream()
                    .map(mapper::toDto)
                    .toList();
        } else throw new AccessDeniedException("Невозможно посмотреть сотрудников чужой компании");
    }
}
