package com.sergeev.taskmanager.company.internal.service;

import com.sergeev.taskmanager.company.api.dto.CompanyDto;
import com.sergeev.taskmanager.company.api.dto.request.CreateCompanyRequest;
import com.sergeev.taskmanager.company.api.dto.request.DeleteCompanyRequest;
import com.sergeev.taskmanager.company.api.dto.request.TransferOwnershipRequest;
import com.sergeev.taskmanager.company.internal.entity.Company;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import com.sergeev.taskmanager.company.internal.entity.CompanyRole;
import com.sergeev.taskmanager.company.internal.entity.PermissionEnum;
import com.sergeev.taskmanager.company.internal.mapper.CompanyMapper;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRoleRepository;
import com.sergeev.taskmanager.exception.BusinessRuleException;
import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import com.sergeev.taskmanager.user.api.UserApi;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyRoleRepository roleRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final CheckPermissionService permissionService;
    private final UserApi userApi;
    private final CompanyMapper mapper;
    private final SecurityFacadeApi securityFacade;

    // Создать компанию
    public CompanyDto createCompany(Long userId, CreateCompanyRequest request) {

        userApi.getUserById(userId);

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
                .name(Company.OWNER)
                .description("Владелец компании")
                .build();

        roleRepository.save(ownerRole);

        CompanyMembership membership = CompanyMembership.builder()
                .userId(userId)
                .company(company)
                .role(ownerRole)
                .build();

        membershipRepository.save(membership);

        return mapper.toDto(company);
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));

        return mapper.toDto(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> getMyCompanies(Long userId) {

        return membershipRepository
                .findAllByUserId(userId)
                .stream()
                .map(CompanyMembership::getCompany)
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public CompanyDto updateCompany(Long companyId, Long userId, CreateCompanyRequest request) {

        permissionService.checkCompanyPermission(
                userId,
                companyId,
                PermissionEnum.MANAGE_COMPANY.getTitle()
        );

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена"));

        company.setName(request.name());
        company.setDescription(request.description());
        company.setEmail(request.email());
        company.setPhone(request.phone());
        company.setAddress(request.address());
        company.setUpdatedAt(LocalDateTime.now());

        return mapper.toDto(company);
    }

    @Transactional
    public void deleteCompany(DeleteCompanyRequest request) {
        Long actorId = securityFacade.getCurrentUserId();
        // Проверяем права
        if (!permissionService.isCompanyOwner(actorId, request.companyId())) {
            throw new AccessDeniedException("Недостаточно прав для удаления этой компании");
        }

        // Проверяем, что компания вообще существует
        if (!companyRepository.existsById(request.companyId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена");
        }

        // Удаляем без select
        companyRepository.deleteById(request.companyId());
    }

    @Transactional
    public void transferOwnership(TransferOwnershipRequest request) throws BusinessRuleException {
        Long currentOwnerId = securityFacade.getCurrentUserId();
        if (permissionService.isCompanyOwner(
                currentOwnerId, request.companyId())) {
            CompanyMembership currentOwner =
                    membershipRepository
                            .findByUserIdAndCompanyId(
                                    currentOwnerId,
                                    request.companyId())
                            .orElseThrow(() ->
                                    new EntityNotFoundException(
                                            "Владелец не найден"));

            CompanyMembership newOwner =
                    membershipRepository
                            .findByUserIdAndCompanyId(
                                    request.newOwnerUserId(),
                                    request.companyId())
                            .orElseThrow(() ->
                                    new EntityNotFoundException(
                                            "Новый владелец не состоит в компании"));

            if (currentOwner.getUserId()
                    .equals(newOwner.getUserId())) {
                throw new BusinessRuleException(
                        "Нельзя передать компанию самому себе");
            }

            CompanyRole ownerRole = roleRepository
                    .findByCompanyIdAndName(
                            request.companyId(),
                            Company.OWNER)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Роль Владельца не найдена"
                            )
                    );

            CompanyRole newRole = roleRepository
                    .findByCompanyIdAndId((request.companyId()), request.newOwnerRoleId())
                    .orElseThrow(() -> new EntityNotFoundException("Роль не найдена"));

            if (Company.OWNER.equals(
                    newRole.getName()
            )) {

                throw new BusinessRuleException(
                        "Нельзя оставить двух владельцев"
                );
            }

            currentOwner.setRole(newRole);
            newOwner.setRole(ownerRole);

        } else throw new AccessDeniedException(
                "Недостаточно прав"
        );
    }
}