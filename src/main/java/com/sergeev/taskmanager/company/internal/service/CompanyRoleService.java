package com.sergeev.taskmanager.company.internal.service;

import com.sergeev.taskmanager.company.api.dto.CompanyRoleDto;
import com.sergeev.taskmanager.company.api.dto.request.AssignRoleRequest;
import com.sergeev.taskmanager.company.api.dto.request.DeleteRoleRequest;
import com.sergeev.taskmanager.company.api.dto.request.RoleRequest;
import com.sergeev.taskmanager.company.internal.entity.*;
import com.sergeev.taskmanager.company.internal.mapper.CompanyRoleMapper;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRoleRepository;
import com.sergeev.taskmanager.company.internal.repository.PermissionRepository;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyRoleService {

    private final CompanyRoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PermissionRepository permissionRepository;
    private final CheckPermissionService permissionService;
    private final SecurityFacadeApi securityFacade;
    private final CompanyRoleMapper mapper;

    // =========================
    // CREATE ROLE
    // =========================
    public void createRole(RoleRequest request) {
        permissionService.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                request.companyId(),
                PermissionEnum.MANAGE_ROLES.name()
        );

        validateRoleName(request.name());

        if (roleRepository.existsByCompanyIdAndName(
                request.companyId(),
                request.name()
        )) {
            throw new IllegalStateException(
                    "Роль с таким названием уже существует"
            );
        }

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Компания не найдена"
                ));

        Set<Permission> permissions =
                resolvePermissions(request.permissions());

        if (permissions.isEmpty()) {
            throw new IllegalStateException(
                    "Роль должна содержать хотя бы одно разрешение"
            );
        }

        CompanyRole role = CompanyRole.builder()
                .company(company)
                .name(request.name().trim().toUpperCase())
                .description(request.description())
                .permissions(permissions)
                .createdAt(LocalDateTime.now())
                .build();

        roleRepository.save(role);
    }


    /**
     * Редактирование роли
     * @param roleId ID редактируемой роли
     * @param request Тело запроса на редактирование роли
     */
    public void updateRole(Long roleId, RoleRequest request) {

        permissionService.checkCompanyPermission(
                securityFacade.getCurrentUserId(),
                request.companyId(),
                PermissionEnum.MANAGE_ROLES.name()
        );

        CompanyRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Роль не найдена"
                ));

        validateRoleBelongsToCompany(role, request.companyId());

        protectOwnerRole(role);

        validateRoleName(request.name());

        boolean roleNameExists =
                roleRepository.existsByCompanyIdAndName(
                        request.companyId(),
                        request.name()
                );

        boolean sameName =
                role.getName().equalsIgnoreCase(request.name());

        if (roleNameExists && !sameName) {
            throw new IllegalStateException(
                    "Роль с таким названием уже существует"
            );
        }

        Set<Permission> permissions =
                resolvePermissions(request.permissions());

        if (permissions.isEmpty()) {
            throw new IllegalStateException(
                    "Роль должна содержать хотя бы одно разрешение"
            );
        }

        role.setName(request.name().trim().toUpperCase());
        role.setDescription(request.description());
        role.setPermissions(permissions);
    }

    // =========================
    // DELETE ROLE
    // =========================
    public void deleteRole(DeleteRoleRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        permissionService.checkCompanyPermission(
                actorId,
                request.companyId(),
                PermissionEnum.MANAGE_ROLES.name()
        );

        CompanyRole role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Роль не найдена"
                ));

        validateRoleBelongsToCompany(role, request.companyId());

        protectOwnerRole(role);

        boolean roleInUse =
                membershipRepository.existsByRoleId(role.getId());

        if (roleInUse) {
            throw new IllegalStateException(
                    "Невозможно удалить роль, которая назначена сотрудникам"
            );
        }

        roleRepository.delete(role);
    }

    // =========================
    // GET ROLES
    // =========================
    @Transactional(readOnly = true)
    public List<CompanyRoleDto> getRoles(
            Long companyId,
            Long userId
    ) {

        permissionService.checkCompanyPermission(
                userId,
                companyId,
                PermissionEnum.VIEW_ROLES.name()
        );

        List<CompanyRole> roles = roleRepository.findAllByCompanyId(companyId);

        return roles.stream()
                .map(mapper::toDto)
                .toList();
    }

    // =========================
    // GET ROLE
    // =========================
    @Transactional(readOnly = true)
    public CompanyRole getRole(
            Long companyId,
            Long roleId,
            Long userId
    ) {

        permissionService.checkCompanyPermission(
                userId,
                companyId,
                PermissionEnum.MANAGE_ROLES.name()
        );

        CompanyRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Роль не найдена"
                ));

        validateRoleBelongsToCompany(role, companyId);

        return role;
    }

    // =========================
    // ASSIGN ROLE
    // =========================
    public void assignRole(AssignRoleRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        permissionService.checkCompanyPermission(
                actorId,
                request.companyId(),
                PermissionEnum.MANAGE_ROLES.name()
        );

        CompanyMembership membership =
                membershipRepository.findById(request.membershipId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Участник компании не найден"
                        ));

        if (!membership.getCompany().getId()
                .equals(request.companyId())) {

            throw new IllegalStateException(
                    "Сотрудник не принадлежит компании"
            );
        }

        protectOwnerMembership(membership);

        CompanyRole role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Роль не найдена"
                ));

        validateRoleBelongsToCompany(role, request.companyId());

        protectOwnerRole(role);

        membership.setRole(role);
    }

    // =========================
    // HELPERS
    // =========================

    private Set<Permission> resolvePermissions(
            Set<PermissionEnum> permissions
    ) {

        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> permissionNames = permissions.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        Set<Permission> entities =
                permissionRepository.findAllByNameIn(permissionNames);

        if (entities.size() != permissionNames.size()) {
            throw new IllegalStateException(
                    "Некоторые permissions не существуют"
            );
        }

        return entities;
    }

    private void validateRoleBelongsToCompany(
            CompanyRole role,
            Long companyId
    ) {

        if (!role.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException(
                    "Роль не принадлежит компании"
            );
        }
    }

    private void validateRoleName(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalStateException(
                    "Название роли обязательно"
            );
        }

        if (name.length() > 100) {
            throw new IllegalStateException(
                    "Название роли слишком длинное"
            );
        }
    }

    private void protectOwnerRole(CompanyRole role) {

        if (Company.OWNER.equals(role.getName())) {
            throw new IllegalStateException(
                    "Роль OWNER защищена"
            );
        }
    }

    private void protectOwnerMembership(
            CompanyMembership membership
    ) {

        if (Company.OWNER.equals(membership.getRole().getName())) {
            throw new IllegalStateException(
                    "Нельзя изменить роль владельца"
            );
        }
    }
}
