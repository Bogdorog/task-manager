package com.sergeev.taskmanager.company.internal.controller;

import com.sergeev.taskmanager.company.api.dto.CompanyDto;
import com.sergeev.taskmanager.company.api.dto.request.*;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import com.sergeev.taskmanager.company.internal.entity.CompanyRole;
import com.sergeev.taskmanager.company.internal.service.CompanyService;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

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
            @RequestBody InviteUserRequest request
    ) {
        service.inviteUser(request);
    }

    // GET MEMBERS
    @GetMapping("/{companyId}/members")
    public List<CompanyMembership> getMembers(@PathVariable Long companyId) {
        return service.getMembers(
                companyId,
                security.getCurrentUserId()
        );
    }

    // REMOVE USER
    @DeleteMapping("/{companyId}/members/{membershipId}")
    public void removeUser(@RequestBody DeleteMemberRequest request) {
        service.removeUser(request);
    }

    // LEAVE COMPANY
    @DeleteMapping("/{companyId}/leave")
    public void leave(@RequestBody LeaveCompanyRequest request) {
        service.leaveCompany(request);
    }

    // GET ROLES
    @GetMapping("/{companyId}/roles")
    public List<CompanyRole> getRoles(@PathVariable Long companyId) {
        return service.getRoles(
                companyId,
                security.getCurrentUserId()
        );
    }

    // CREATE ROLE
    @PostMapping("/{companyId}/roles")
    public void createRole(
            @PathVariable Long companyId,
            @RequestBody CreateRoleRequest request
    ) {
        service.createRole(
                request
        );
    }

    // =========================
    // DELETE ROLE
    // =========================
    @DeleteMapping("/{companyId}/roles/{roleId}")
    public void deleteRole(
            @PathVariable Long companyId,
            @RequestBody DeleteRoleRequest request
    ) {
        service.deleteRole(request);
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
        service.assignRole(request);
    }

    // =========================
    // TRANSFER OWNER
    // =========================
    @PutMapping("/{companyId}/transfer-owner/{userId}")
    public void transferOwner(
            @PathVariable Long companyId,
            @RequestBody TransferOwnershipRequest request
    ) {
        service.transferOwnership(request);
    }
}
