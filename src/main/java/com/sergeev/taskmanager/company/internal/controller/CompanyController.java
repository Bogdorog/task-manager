package com.sergeev.taskmanager.company.internal.controller;

import com.sergeev.taskmanager.company.api.dto.*;
import com.sergeev.taskmanager.company.api.dto.request.*;
import com.sergeev.taskmanager.company.internal.service.CompanyMembershipService;
import com.sergeev.taskmanager.company.internal.service.CompanyRoleService;
import com.sergeev.taskmanager.company.internal.service.CompanyService;
import com.sergeev.taskmanager.exception.BusinessRuleException;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyMembershipService membershipService;
    private final CompanyRoleService roleService;
    private final CompanyService service;
    private final SecurityFacadeApi security;

    @PostMapping
    @Operation(summary = "Создать компанию")
    public CompanyDto create(@Valid @RequestBody CreateCompanyRequest request) {
        return service.createCompany(
                security.getCurrentUserId(),
                request
        );
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Получить информацию о компании")
    public CompanyDto get(@PathVariable Long companyId) {
        return service.getCompany(companyId);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить информацию о собственных компаниях")
    public List<CompanyDto> getMyCompanies() {

        return service.getMyCompanies(
                security.getCurrentUserId()
        );
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Обновить информацию о компании")
    public CompanyDto update(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateCompanyRequest request
    ) {
        return service.updateCompany(
                companyId,
                security.getCurrentUserId(),
                request
        );
    }

    @DeleteMapping("/{companyId}")
    @Operation(summary = "Удалить компанию")
    public void delete(@RequestBody DeleteCompanyRequest request) {
        service.deleteCompany(request);
    }

    @PostMapping("/{companyId}/members")
    @Operation(summary = "Добавить сотрудника в компанию")
    public void inviteUser(
            @PathVariable Long companyId,
            @RequestBody InviteUserRequest request
    ) {
        InviteUserRequest updatedRequest =
                new InviteUserRequest(
                        companyId,
                        request.user(),
                        request.roleId());
        membershipService.inviteUser(updatedRequest);
    }

    @GetMapping("/{companyId}/members")
    @Operation(summary = "Получить информацию о компании")
    public List<CompanyMembershipDto> getMembers(@PathVariable Long companyId) {
        return membershipService.getMembers(
                companyId,
                security.getCurrentUserId()
        );
    }

    @GetMapping("/{companyId}/members/short")
    @Operation(summary = "Получить список сотрудников для отображения в окнах выбора")
    public List<ShortCompanyMembershipDto> getShortMembers(@PathVariable Long companyId) {
        return membershipService.getShortMembers(
                companyId,
                security.getCurrentUserId()
        );
    }

    @DeleteMapping("/{companyId}/members/{membershipId}")
    @Operation(summary = "Удалить сотрудника из компании")
    public void removeUser(@PathVariable Long companyId,
                           @PathVariable Long membershipId) throws BusinessRuleException {
        DeleteMemberRequest request =
                new DeleteMemberRequest(
                        companyId,
                        membershipId);
        membershipService.removeUser(request);
    }

    @DeleteMapping("/{companyId}/leave")
    @Operation(summary = "Покинуть компанию")
    public void leave(@PathVariable Long companyId) throws BusinessRuleException {
        LeaveCompanyRequest request =
                new LeaveCompanyRequest(
                        companyId);
        membershipService.leaveCompany(request);
    }

    @GetMapping("/{companyId}/roles")
    @Operation(summary = "Получить инфомацию о ролях компании")
    public List<CompanyRoleDto> getRoles(@PathVariable Long companyId) {
        return roleService.getRoles(
                companyId,
                security.getCurrentUserId()
        );
    }

    @PostMapping("/{companyId}/roles")
    @Operation(summary = "Добавить роль в компанию")
    public void createRole(
            @PathVariable Long companyId,
            @RequestBody RoleRequest request
    ) {
        roleService.createRole(request);
    }

    @PutMapping("/{companyId}/role/{roleId}")
    @Operation(summary = "Изменить роль компании")
    public void updateRole(
            @PathVariable Long roleId,
            @RequestBody RoleRequest request
    ) {
        roleService.updateRole(roleId, request);
    }

    @DeleteMapping("/{companyId}/roles/{roleId}")
    @Operation(summary = "Удалить роль компании")
    public void deleteRole(
            @PathVariable Long companyId,
            @PathVariable Long roleId
    ) {
        DeleteRoleRequest request =
                new DeleteRoleRequest(
                        companyId,
                        roleId);
        roleService.deleteRole(request);
    }

    @PutMapping("/{companyId}/members/{membershipId}/role")
    @Operation(summary = "Назначить сотруднику новую роль")
    public void assignRole(
            @PathVariable Long companyId,
            @PathVariable Long membershipId,
            @RequestBody AssignRoleRequest request
    ) {
        AssignRoleRequest updatedRequest =
                new AssignRoleRequest(
                        companyId,
                        membershipId,
                        request.roleId());
        roleService.assignRole(updatedRequest);
    }

    @PutMapping("/{companyId}/transfer-owner/{userId}")
    @Operation(summary = "Назначить нового владельца компании компании")
    public void transferOwner(
            @PathVariable Long companyId,
            @PathVariable Long userId,
            @RequestBody TransferOwnershipRequest request
    ) throws BusinessRuleException {
        TransferOwnershipRequest updatedRequest =
                new TransferOwnershipRequest(
                        companyId,
                        userId,
                        request.newOwnerRoleId());
        service.transferOwnership(updatedRequest);
    }

    @GetMapping("/{companyId}/permissions/me")
    @Operation(summary = "Получить список разрешенных действий сотрудника компании")
    public CompanyPermissionsDto getMyPermissions(
            @PathVariable Long companyId
    ) {
        return membershipService.getCurrentUserPermissions(companyId);
    }
}
