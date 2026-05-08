package com.sergeev.taskmanager.company.internal;

import com.sergeev.taskmanager.company.api.dto.CompanyDto;
import com.sergeev.taskmanager.company.api.dto.request.AssignRoleRequest;
import com.sergeev.taskmanager.company.api.dto.request.CreateCompanyRequest;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import com.sergeev.taskmanager.company.internal.entity.CompanyRole;
import com.sergeev.taskmanager.company.internal.mapper.CompanyMapper;
import com.sergeev.taskmanager.company.internal.repository.CompanyMembershipRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRepository;
import com.sergeev.taskmanager.company.internal.repository.CompanyRoleRepository;
import com.sergeev.taskmanager.company.internal.service.CompanyService;
import com.sergeev.taskmanager.user.api.UserApi;
import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.internal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyRoleRepository companyRoleRepository;
    @Mock
    private CompanyMembershipRepository companyMembershipRepository;
    @Mock
    private UserApi userApi;
    @Mock
    private CompanyMapper companyMapper;
    @InjectMocks
    private CompanyService companyService;

    public final String OWNER_ROLE = "OWNER";
    private CreateCompanyRequest request = new CreateCompanyRequest(
            "Test Company",
            "desc",
            "mail@test.com",
            "+123",
            "addr"
    );

    @Test
    void shouldCreateCompanyWithOwner() {

        Long userId = 1L;

        UserDto user = new UserDto(userId, null, null, null, null, null, null, null);

        when(userApi.getUserById(userId))
                .thenReturn(user);

        when(companyRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        when(companyRoleRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        when(companyMembershipRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        when(companyMapper.toResponse(any()))
                .thenReturn(new CompanyDto(1L, request.name(), request.description(), request.email(), request.phone(), request.address()));

        ArgumentCaptor<CompanyRole> roleCaptor =
                ArgumentCaptor.forClass(CompanyRole.class);


        CompanyDto result = companyService.createCompany(userId, request);
        // Проверяем назначенную роль
        verify(companyRoleRepository).save(roleCaptor.capture());
        CompanyRole role = roleCaptor.getValue();
        assertEquals(OWNER_ROLE, role.getName());
        // Проверяем правильность выходных данных
        assertNotNull(result);
        assertEquals(request.name(), result.name());
        // Проверяем, что все данные были сохранены
        verify(companyRepository).save(any());
        verify(companyRoleRepository).save(any());
        verify(companyMembershipRepository).save(any());
    }

    @Test
    void shouldFailIfUserNotFound() {
        when(userApi.getUserById(1L))
                .thenReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                companyService.createCompany(1L, request)
        );
    }

    @Test
    void shouldNotDeleteOwnerRole() {

        CompanyRole role = new CompanyRole();
        role.setName(OWNER_ROLE);

        when(companyRoleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        assertThrows(IllegalStateException.class, () ->
                companyService.deleteRole(1L)
        );
    }

    @Test
    void shouldNotAssignOwnerRole() {

        CompanyRole role = new CompanyRole();
        role.setName(OWNER_ROLE);

        CompanyMembership companyMembership = new CompanyMembership();
        companyMembership.setId(2L);

        when(companyMembershipRepository.findById(2L))
                .thenReturn(Optional.of(companyMembership));

        when(companyRoleRepository.findById(10L))
                .thenReturn(Optional.of(role));

        assertThrows(IllegalStateException.class, () ->
                companyService.assignRole(1L, new AssignRoleRequest(2L, 10L))
        );
    }
}
