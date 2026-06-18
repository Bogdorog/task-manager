package com.sergeev.taskmanager.company.internal.service;

import com.sergeev.taskmanager.company.api.dto.CompanyMembershipDto;
import com.sergeev.taskmanager.company.api.dto.CompanyPermissionsDto;
import com.sergeev.taskmanager.company.api.dto.ShortCompanyMembershipDto;
import com.sergeev.taskmanager.company.api.dto.request.DeleteMemberRequest;
import com.sergeev.taskmanager.company.api.dto.request.InviteUserRequest;
import com.sergeev.taskmanager.company.api.dto.request.LeaveCompanyRequest;
import com.sergeev.taskmanager.company.internal.entity.Company;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import com.sergeev.taskmanager.company.internal.entity.CompanyRole;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.company.internal.mapper.CompanyMembershipMapper;
import com.sergeev.taskmanager.company.internal.mapper.CompanyPermissionMapper;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRoleRepository;
import com.sergeev.taskmanager.exception.BusinessRuleException;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import com.sergeev.taskmanager.user.api.UserApi;
import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.UserShortDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final CompanyMembershipMapper membershipMapper;
    private final CompanyPermissionMapper permissionMapper;
    private final SecurityFacadeApi securityFacade;

    public void inviteUser(InviteUserRequest request) {
        permissionService.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                request.companyId(),
                PermissionEnum.INVITE_USER.getTitle()
        );
        UserDto user = userApi.getUser(request.user());

        boolean alreadyMember = membershipRepository.existsByUserIdAndCompanyId(
                user.id(),
                request.companyId()
        );

        if (alreadyMember)
        {
            throw new BusinessRuleException("Пользователь уже состоит в компании");
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

        if (Company.OWNER.equals(role.getName()))
        {
            throw new BusinessRuleException("Нельзя назначить роль владельца");
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
                        .userId(user.id())
                        .company(company)
                        .role(role)
                        .joinedAt(LocalDateTime.now())
                        .build();

        membershipRepository.save(membership);
    }

    public void removeUser(
            DeleteMemberRequest request
    ) throws BusinessRuleException {
        Long actorId = securityFacade.getCurrentUserId();
        permissionService.checkCompanyPermission(
                actorId,
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
                .equals(actorId)) {
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
                                securityFacade.getCurrentUserId(),
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

    /**
     * Получение списка участников компании.
     */
    @Transactional(readOnly = true)
    public List<CompanyMembershipDto> getMembers(Long companyId, Long actorId) {
        if (!permissionService.isCompanyMember(actorId, companyId)) {
            throw new AccessDeniedException("Невозможно посмотреть сотрудников чужой компании");
        }

        List<CompanyMembership> memberships = membershipRepository.findAllByCompanyId(companyId);

        // Пакетно собираем все user
        Set<Long> userIds = memberships.stream()
                .map(CompanyMembership::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> usersMap = userApi.getUsersByIds(userIds);

        // Маппинг + обогащение
        return memberships.stream()
                .map(membership -> {
                    CompanyMembershipDto base = membershipMapper.toDto(membership);
                    UserShortDto user = membership.getUserId() != null
                            ? usersMap.get(membership.getUserId())
                            : null;
                    return new CompanyMembershipDto(
                            base.id(), user, base.role(), base.joinedAt()
                    );
                })
                .toList();
    }

    /**
     * Получение короткого (служебного) списка участников компании.
     */
    @Transactional(readOnly = true)
    public List<ShortCompanyMembershipDto> getShortMembers(Long companyId, Long actorId) {
        if (!permissionService.isCompanyMember(actorId, companyId)) {
            throw new AccessDeniedException("Невозможно посмотреть сотрудников чужой компании");
        }

        List<CompanyMembership> memberships = membershipRepository.findAllByCompanyId(companyId);

        // Пакетно собираем все user
        Set<Long> userIds = memberships.stream()
                .map(CompanyMembership::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> usersMap = userApi.getUsersByIds(userIds);

        // Маппинг + обогащение
        return memberships.stream()
                .map(membership -> {
                    ShortCompanyMembershipDto base = membershipMapper.toShortDto(membership);
                    UserShortDto user = membership.getUserId() != null
                            ? usersMap.get(membership.getUserId())
                            : null;
                    return new ShortCompanyMembershipDto(
                            base.id(), user.fullName(), base.role());
                })
                .toList();
    }

    /**
     * Получение информации о себе как сотруднике компании.
     */
    public CompanyMembershipDto getMembership(Long companyId) {
        CompanyMembership membership = membershipRepository
                .findByUserIdAndCompanyId(securityFacade.getCurrentUserId(), companyId)
                .orElseThrow(() -> new AccessDeniedException("Пользователь не состоит в компании"));

        CompanyMembershipDto base = membershipMapper.toDto(membership);

        // Обогащение пользователем
        UserShortDto user = membership.getUserId() != null
                ? userApi.getShortUserById(membership.getUserId())
                : null;

        return new CompanyMembershipDto(
                base.id(), user, base.role(), base.joinedAt()
        );
    }

    @Transactional(readOnly = true)
    public CompanyPermissionsDto getCurrentUserPermissions (Long companyId)
    {
        CompanyMembershipDto membership = getMembership(companyId);
        if (membership.role().name().equals(Company.OWNER))
        {
            return permissionMapper.ownerToDto();
        } else return permissionMapper.toDto(membership.role().permissions());
    }
}
