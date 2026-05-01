package com.sergeev.taskmanager.user.internal;

import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.request.*;
import com.sergeev.taskmanager.user.api.event.PasswordResetRequestedEvent;
import com.sergeev.taskmanager.user.internal.entity.PasswordResetToken;
import com.sergeev.taskmanager.user.internal.entity.Role;
import com.sergeev.taskmanager.user.internal.entity.User;
import com.sergeev.taskmanager.user.internal.mapper.UserMapper;
import com.sergeev.taskmanager.user.internal.repository.PasswordResetTokenRepository;
import com.sergeev.taskmanager.user.internal.repository.UserRepository;
import com.sergeev.taskmanager.user.internal.service.PasswordChangeService;
import com.sergeev.taskmanager.user.internal.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordChangeService passwordChangeService;

    @InjectMocks
    private UserService userService;

    private Long userId;
    private User activeUser;

    UserServiceTest() {}

    @BeforeEach
    void setUp() {
        SecureRandom secureRandom = new SecureRandom();
        userId = Math.abs(secureRandom.nextLong());
        Role role = new Role(2L, "USER");
        activeUser = new User(
                userId,
                "login",
                "John Doe",
                role,
                null,
                "Russia",
                "test@mail.com",
                "+12345678901",
                "encoded-password",
                true,
                null,
                null
        );
    }
    // =============================
    // Вход позитивные тесты
    // =============================
    @Test
    void shouldLoginByLoginSuccessfully() {

        when(repository.findByLogin("login"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches("raw-password", "encoded-password"))
                .thenReturn(true);

        Assertions.assertDoesNotThrow(() ->
                userService.login(new LoginRequest("login", "raw-password"))
        );
    }
    /* Тесты на вход, пока что вход только через логин
    @Test
    void shouldLoginByEmailSuccessfully() {

        when(repository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches("raw-password", "encoded-password"))
                .thenReturn(true);

        Assertions.assertDoesNotThrow(() ->
                userService.login(new LoginRequest("test@mail.com", "raw-password"))
        );
    }

    @Test
    void shouldLoginByPhoneSuccessfully() {

        when(repository.findByPhone("+12345678901"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches("raw-password", "encoded-password"))
                .thenReturn(true);

        Assertions.assertDoesNotThrow(() ->
                userService.login(new LoginRequest("+12345678901", "raw-password"))
        );
    }
     */
    // =============================
    // Вход негативные тесты
    // =============================
    @Test
    void shouldThrowIfUserNotFound() {

        when(repository.findByLogin("unknownlogin"))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResponseStatusException.class, () ->
                userService.login(new LoginRequest("unknownlogin", "pass"))
        );
    }

    @Test
    void shouldThrowIfPasswordInvalid() {

        when(repository.findByLogin("login"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches("wrong", "encoded-password"))
                .thenReturn(false);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.login(new LoginRequest("login", "wrong"))
        );
    }

    @Test
    void shouldThrowIfUserInactive() {
        Role role = new Role(2L, "USER");
        User inactiveUser = new User(
                userId,
                "login",
                "John Doe",
                role,
                null,
                "Russia",
                "test@mail.com",
                "+12345678901",
                "encoded-password",
                false,
                null,
                null
        );

        when(repository.findByLogin("login"))
                .thenReturn(Optional.of(inactiveUser));

        when(passwordEncoder.matches("raw-password", "encoded-password"))
                .thenReturn(true);

        Assertions.assertThrows(IllegalStateException.class, () ->
                userService.login(new LoginRequest("login", "raw-password"))
        );
    }
    // =============================
    // Регистрация
    // =============================
    @Test
    void shouldRegisterSuccessfully() {
        RegisterUserRequest request = new RegisterUserRequest(
                "login",
                "John",
                "+12345678901",
                "test@mail.com",
                "addr",
                "pass"
        );

        when(repository.existsByEmail("test@mail.com")).thenReturn(false);
        when(repository.existsByPhone("+12345678901")).thenReturn(false);
        when(repository.existsByLogin("login")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hash");

        User savedUser = User.builder().id(1L).build();
        when(repository.save(any())).thenReturn(savedUser);
        when(userMapper.toResponse(any()))
                .thenReturn(new UserDto(1L, "login", null, null, null, null, null, null));
        UserDto userDto = userService.register(request);
        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(1L, userDto.id());
    }

    @Test
    void shouldThrowIfUserAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest(
                "login",
                "John",
                "+12345678901",
                "test@mail.com",
                "addr",
                "pass"
        );

        when(repository.existsByEmail("test@mail.com")).thenReturn(true);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(request));
    }

    // Редактирование профиля
    @Test
    void shouldUpdateProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "login",
                "New Name",
                "+999",
                "test@mail.com",
                "New addr"
        );

        when(repository.findByLogin("login"))
                .thenReturn(Optional.of(activeUser));

        when(userMapper.toResponse(any()))
                .thenReturn(new UserDto(activeUser.getId(),
                        activeUser.getLogin(),
                        activeUser.getFullName(),
                        activeUser.getPhone(),
                        activeUser.getEmail(),
                        activeUser.getAddress(),
                        activeUser.getRole().getName(),
                        null));
        UserDto dto = userService.updateProfile(request);

        Assertions.assertNotNull(dto);
    }

    @Test
    void shouldGetUser() {
        when(repository.findByLogin("login"))
                .thenReturn(Optional.of(activeUser));

        when(userMapper.toResponse(activeUser))
                .thenReturn(new UserDto(activeUser.getId(),
                        activeUser.getLogin(),
                        activeUser.getFullName(),
                        activeUser.getPhone(),
                        activeUser.getEmail(),
                        activeUser.getAddress(),
                        activeUser.getRole().getName(),
                        null));

        UserDto dto = userService.get("login");

        Assertions.assertNotNull(dto);
    }
    // =============================
    // Смена пароля
    // =============================
    // Позитивные
    @Test
    void shouldInitiatePasswordReset() {
        when(repository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(activeUser));

        when(passwordChangeService.generateToken()).thenReturn("raw");
        when(passwordChangeService.hashToken("raw")).thenReturn("hash");

        userService.initiatePasswordReset(
                new PasswordResetRequest("test@mail.com")
        );

        verify(tokenRepository).save(any());
        verify(publisher).publishEvent(any(PasswordResetRequestedEvent.class));
    }

    @Test
    void shouldConfirmPasswordReset() {
        PasswordResetToken token = mock(PasswordResetToken.class);

        when(passwordChangeService.hashToken("token")).thenReturn("hash");
        when(tokenRepository.findByTokenHash("hash"))
                .thenReturn(Optional.of(token));
        when(token.isExpired()).thenReturn(false);
        when(token.isUsed()).thenReturn(false);

        when(repository.findById(userId))
                .thenReturn(Optional.of(activeUser));

        when(token.getUserId()).thenReturn(userId);
        when(passwordEncoder.encode("new")).thenReturn("encoded");

        userService.confirmPasswordReset(
                new PasswordResetConfirmRequest("token", "new")
        );

        verify(tokenRepository).invalidateAllForUser(userId);
        verify(repository).save(any());
    }
    // Негативные
    @Test
    void shouldThrowIfUserNotFoundOnReset() {
        when(repository.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResponseStatusException.class, () ->
                userService.initiatePasswordReset(
                        new PasswordResetRequest("test@mail.com")
                ));
    }

    @Test
    void shouldThrowIfTokenInvalid() {
        when(passwordChangeService.hashToken("token")).thenReturn("hash");
        when(tokenRepository.findByTokenHash("hash"))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.confirmPasswordReset(
                        new PasswordResetConfirmRequest("token", "new")
                ));
    }

    @Test
    void shouldThrowIfTokenExpired() {
        PasswordResetToken token = mock(PasswordResetToken.class);

        when(passwordChangeService.hashToken("token")).thenReturn("hash");
        when(tokenRepository.findByTokenHash("hash"))
                .thenReturn(Optional.of(token));

        when(token.isExpired()).thenReturn(true);

        Assertions.assertThrows(IllegalStateException.class, () ->
                userService.confirmPasswordReset(
                        new PasswordResetConfirmRequest("token", "new")
                ));
    }

    @Test
    void shouldThrowIfTokenUsed() {
        PasswordResetToken token = mock(PasswordResetToken.class);

        when(passwordChangeService.hashToken("token")).thenReturn("hash");
        when(tokenRepository.findByTokenHash("hash"))
                .thenReturn(Optional.of(token));

        when(token.isExpired()).thenReturn(false);
        when(token.isUsed()).thenReturn(true);

        Assertions.assertThrows(IllegalStateException.class, () ->
                userService.confirmPasswordReset(
                        new PasswordResetConfirmRequest("token", "new")
                ));
    }
    // =============================
    // Удаление аккаунта
    // =============================
    /*
    @Test
    void shouldCreateDeletionToken() {
        var user = new User();

        when(userRepository.findByEmail("mail@test.com"))
                .thenReturn(Optional.of(user));

        userService.requestAccountDeletion("mail@test.com");

        verify(passwordResetTokenRepository).save(any());
    }
    @Test
    void shouldDeleteUserIfCodeValid() {
        var token = new PasswordResetToken();
        token.setUsed(false);

        when(passwordResetTokenRepository.findByTokenHash(any()))
                .thenReturn(Optional.of(token));

        userService.confirmDeletion("code");

        verify(userRepository).delete(any());
    }
     */
}
