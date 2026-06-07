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

    // CREATE COMPANY
    @PostMapping
    public CompanyDto create(@Valid @RequestBody CreateCompanyRequest request) {
        return service.createCompany(
                security.getCurrentUserId(),
                request
        );
    }

    // GET COMPANY
    @GetMapping("/{companyId}")
    public CompanyDto get(@PathVariable Long companyId) {
        return service.getCompany(companyId);
    }

    @GetMapping("/my")
    public List<CompanyDto> getMyCompanies() {

        return service.getMyCompanies(
                security.getCurrentUserId()
        );
    }

    // UPDATE COMPANY
    @PutMapping("/{companyId}")
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

    // DELETE COMPANY
    @DeleteMapping("/{companyId}")
    public void delete(@RequestBody DeleteCompanyRequest request) {
        service.deleteCompany(request);
    }

    // INVITE USER
    @PostMapping("/{companyId}/members")
    public void inviteUser(
            @PathVariable Long companyId,
            @RequestBody InviteUserRequest request
    ) throws BusinessRuleException {
        InviteUserRequest updatedRequest =
                new InviteUserRequest(
                        companyId,
                        request.userId(),
                        request.roleId());
        membershipService.inviteUser(updatedRequest);
    }

    // GET MEMBERS
    @GetMapping("/{companyId}/members")
    public List<CompanyMembershipDto> getMembers(@PathVariable Long companyId) {
        return membershipService.getMembers(
                companyId,
                security.getCurrentUserId()
        );
    }

    /**
     *  Получение списка сотрудников для отображения в окнах выбора.
     */
    @GetMapping("/{companyId}/members/short")
    public List<ShortCompanyMembershipDto> getShortMembers(@PathVariable Long companyId) {
        return membershipService.getShortMembers(
                companyId,
                security.getCurrentUserId()
        );
    }

    // REMOVE USER
    @DeleteMapping("/{companyId}/members/{membershipId}")
    public void removeUser(@PathVariable Long companyId,
                           @PathVariable Long membershipId) throws BusinessRuleException {
        DeleteMemberRequest request =
                new DeleteMemberRequest(
                        companyId,
                        membershipId);
        membershipService.removeUser(request);
    }

    // LEAVE COMPANY
    @DeleteMapping("/{companyId}/leave")
    public void leave(@PathVariable Long companyId) throws BusinessRuleException {
        LeaveCompanyRequest request =
                new LeaveCompanyRequest(
                        companyId);
        membershipService.leaveCompany(request);
    }

    // GET ROLES
    @GetMapping("/{companyId}/roles")
    public List<CompanyRoleDto> getRoles(@PathVariable Long companyId) {
        return roleService.getRoles(
                companyId,
                security.getCurrentUserId()
        );
    }

    // CREATE ROLE
    @PostMapping("/{companyId}/roles")
    public void createRole(
            @PathVariable Long companyId,
            @RequestBody RoleRequest request
    ) {
        roleService.createRole(
                request
        );
    }

    // =========================
    // DELETE ROLE
    // =========================
    @DeleteMapping("/{companyId}/roles/{roleId}")
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

    // =========================
    // ASSIGN ROLE
    // =========================
    @PutMapping("/{companyId}/members/{membershipId}/role")
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

    // =========================
    // TRANSFER OWNER
    // =========================
    @PutMapping("/{companyId}/transfer-owner/{userId}")
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
    @Operation(summary = "Возможности пользователя")
    public CompanyPermissionsDto getMyPermissions(
            @PathVariable Long companyId
    ) {
        return membershipService.getCurrentUserPermissions(companyId);
    }
}
