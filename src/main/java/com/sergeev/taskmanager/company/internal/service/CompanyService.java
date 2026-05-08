package com.sergeev.taskmanager.company.internal.service;

import com.sergeev.taskmanager.company.api.dto.CompanyDto;
import com.sergeev.taskmanager.company.api.dto.request.*;
import com.sergeev.taskmanager.company.internal.entity.Company;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import com.sergeev.taskmanager.company.internal.entity.CompanyRole;
import com.sergeev.taskmanager.company.internal.mapper.CompanyMapper;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRoleRepository;
import com.sergeev.taskmanager.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.sergeev.taskmanager.company.internal.entity.PermissionEnum.INVITE_USER;
import static com.sergeev.taskmanager.company.internal.entity.PermissionEnum.MANAGE_COMPANY;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyRoleRepository roleRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final UserApi userApi;
    private final CompanyMapper mapper;

    public static final String OWNER = "OWNER";

    // Создать компанию
    public CompanyDto createCompany(Long userId, CreateCompanyRequest request) {

        if (userApi.getUserById(userId) == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }

        Company company = Company.builder()
                .name(request.name())
                .description(request.description())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .build();

        company = companyRepository.save(company);

        CompanyRole ownerRole = CompanyRole.builder()
                .company(company)
                .name(OWNER)
                .description("Владелец компании")
                .build();

        ownerRole = roleRepository.save(ownerRole);

        CompanyMembership membership = CompanyMembership.builder()
                .userId(userId)
                .company(company)
                .role(ownerRole)
                .build();

        membershipRepository.save(membership);

        return mapper.toResponse(company);
    }

    // Пригласить пользователя
    public void inviteUser(InviteUserRequest request) {

        // Проверка членства
        CompanyMembership inviter = membershipRepository
                .findByUserIdAndCompanyId(request.inviterId(), request.companyId())
                .orElseThrow(() -> new IllegalStateException("Приглашающий не является сотрудником данной компании"));

        // Проверка прав
        if (!inviter.getRole().hasPermission(INVITE_USER)) {
            throw new IllegalStateException("У сотрудника нет полномочий на данное дествие");
        }

        // пользователь уже в компании?
        if (membershipRepository.existsByUserIdAndCompanyId(request.userId(), request.companyId())) {
            throw new IllegalStateException("Пользователь уже в компании");
        }

        CompanyRole role = roleRepository.findByCompanyIdAndId(request.companyId(), request.roleId())
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));

        // критическая проверка
        if (!role.getCompany().getId().equals(request.companyId())) {
            throw new IllegalStateException("Роль не найдена");
        }

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow();

        CompanyMembership membership = CompanyMembership.builder()
                .userId(request.userId())
                .company(company)
                .role(role)
                .build();

        membershipRepository.save(membership);
    }
    
    // Назначить роль сотруднику
    public void assignRole(AssignRoleRequest request) {

        // Проверка членства
        CompanyMembership actor = membershipRepository
                .findByUserIdAndCompanyId(request.actorId(), request.companyId())
                .orElseThrow(() -> new IllegalStateException("Назначающий не является сотрудником данной компании"));

        // Проверка прав
        if (!actor.getRole().hasPermission(MANAGE_COMPANY)) {
            throw new IllegalStateException("У сотрудника нет полномочий на данное дествие");
        }

        CompanyMembership membership = membershipRepository.findById(request.membershipId())
                .orElseThrow();

        CompanyRole newRole = roleRepository.findById(request.roleId())
                .orElseThrow();

        // роль из той же компании
        if (!membership.getCompany().getId().equals(newRole.getCompany().getId())) {
            throw new IllegalStateException("Роль не найдена");
        }

        // нельзя назначить OWNER
        if (newRole.getName().equals(OWNER)) {
            throw new IllegalStateException("Нельзя назначить роль владельца");
        }

        // нельзя менять OWNER
        if (membership.getRole().getName().equals(OWNER)) {
            throw new IllegalStateException("Нельзя сменить владельца");
        }

        membership.setRole(newRole);
    }

    
    // Создать роль
    public void createRole(CreateRoleRequest request) {

        // Проверка членства
        CompanyMembership actor = membershipRepository
                .findByUserIdAndCompanyId(request.actorId(), request.companyId())
                .orElseThrow(() -> new IllegalStateException("Назначающий не является сотрудником данной компании"));

        // Проверка прав
        if (!actor.getRole().hasPermission(MANAGE_COMPANY)) {
            throw new IllegalStateException("У сотрудника нет полномочий на данное дествие");
        }

        if (roleRepository.existsByCompanyIdAndName(request.companyId(), request.name())) {
            throw new IllegalStateException("Данная роль уже существует");
        }

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow();

        CompanyRole role = CompanyRole.builder()
                .company(company)
                .name(request.name())
                .description(request.description())
                .build();

        roleRepository.save(role);
    }

    
    // Удалить роль
    public void deleteRole(DeleteRoleRequest request) {
        // Проверка членства
        CompanyMembership actor = membershipRepository
                .findByUserIdAndCompanyId(request.actorId(), request.companyId())
                .orElseThrow(() -> new IllegalStateException("Назначающий не является сотрудником данной компании"));

        // Проверка прав
        if (!actor.getRole().hasPermission(MANAGE_COMPANY)) {
            throw new IllegalStateException("У сотрудника нет полномочий на данное дествие");
        }

        CompanyRole role = roleRepository.findById(request.roleId())
                .orElseThrow();

        if (role.getName().equals(OWNER)) {
            throw new IllegalStateException("Невозможно удалить роль владельца");
        }

        if (membershipRepository.existsByRoleId(request.roleId())) {
            throw new IllegalStateException("Невозможно удалить роль");
        }

        roleRepository.delete(role);
    }

    
    // Удалить сотрудника
    public void removeUser(DeleteMemberRequest request) {
        // Проверка членства
        CompanyMembership actor = membershipRepository
                .findByUserIdAndCompanyId(request.actorId(), request.companyId())
                .orElseThrow(() -> new IllegalStateException("Назначающий не является сотрудником данной компании"));

        // Проверка прав
        if (!actor.getRole().hasPermission(MANAGE_COMPANY)) {
            throw new IllegalStateException("У сотрудника нет полномочий на данное дествие");
        }

        CompanyMembership membership = membershipRepository.findById(request.membershipId())
                .orElseThrow();

        if (membership.getRole().getName().equals(OWNER)) {
            throw new IllegalStateException("Невозможно уволить владельца");
        }

        membershipRepository.delete(membership);
    }

    
    // Покинуть компанию
    public void leaveCompany(LeaveCompanyRequest request) {
        // Проверка членства
        CompanyMembership actor = membershipRepository
                .findByUserIdAndCompanyId(request.actorId(), request.companyId())
                .orElseThrow(() -> new IllegalStateException("Назначающий не является сотрудником данной компании"));

        if (actor.getRole().getName().equals(OWNER)) {
            throw new IllegalStateException("Владелец не может покинуть компанию");
        }

        membershipRepository.delete(actor);
    }

    
    // Получить всех сотрудников
    public List<CompanyMembership> getMembers(Long companyId, Long userId) {

        if (!membershipRepository.existsByUserIdAndCompanyId(userId, companyId)) {
            throw new IllegalStateException("Действие невозможно выполнить");
        }

        return membershipRepository.findAllByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));

        return mapper.toResponse(company);
    }

    public CompanyDto updateCompany(Long companyId, Long userId, CreateCompanyRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));

        company.setName(request.name());
        company.setDescription(request.description());
        company.setEmail(request.email());
        company.setPhone(request.phone());
        company.setAddress(request.address());

        return mapper.toResponse(company);
    }

    public void deleteCompany(DeleteCompanyRequest request) {
        // Проверка членства
        CompanyMembership actor = membershipRepository
                .findByUserIdAndCompanyId(request.actorId(), request.companyId())
                .orElseThrow(() -> new IllegalStateException("Назначающий не является сотрудником данной компании"));
        checkOwner(actor);
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));

        companyRepository.delete(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyRole> getRoles(Long companyId, Long userId) {
        return roleRepository.findAllByCompanyId(companyId);
    }

    @Transactional
    public void transferOwnership(TransferOwnershipRequest request) {

        CompanyMembership currentOwner = getMembership(request.newOwnerUserId(), request.companyId());
        checkOwner(currentOwner);

        CompanyMembership newOwner = getMembership(request.newOwnerUserId(), request.companyId());

        CompanyRole ownerRole = roleRepository
                .findByCompanyIdAndName(request.companyId(), OWNER)
                .orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        // Текущий владелец меняет роль на указанную
        CompanyRole managerRole = roleRepository
                .findByCompanyIdAndId((request.companyId()), request.newOwnerRoleId())
                .orElseThrow(() -> new IllegalStateException("Роль не найдена"));

        currentOwner.setRole(managerRole);
        newOwner.setRole(ownerRole);
    }

    @Transactional(readOnly = true)
    public CompanyMembership getMembership(Long membershipId, Long userId) {

        CompanyMembership membership = membershipRepository.findByUserIdAndCompanyId(membershipId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник компании не найден"));

        return membership;
    }

    private void checkOwner(CompanyMembership membership) {
        if (!membership.getRole().getName().equals(OWNER)) {
            throw new IllegalStateException("Доступно только владельцу");
        }
    }
}