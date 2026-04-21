package com.sergeev.taskmanager.user.internal.service;

import com.sergeev.taskmanager.media.api.MediaApi;
import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.request.*;
import com.sergeev.taskmanager.user.api.event.PasswordResetRequestedEvent;
import com.sergeev.taskmanager.user.internal.entity.PasswordResetToken;
import com.sergeev.taskmanager.user.internal.entity.Role;
import com.sergeev.taskmanager.user.internal.entity.User;
import com.sergeev.taskmanager.user.internal.mapper.UserMapper;
import com.sergeev.taskmanager.user.internal.repository.PasswordResetTokenRepository;
import com.sergeev.taskmanager.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository repository;
    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordChangeService passwordChangeService;
    private final MediaApi mediaApi;

    @Transactional
    @CachePut(value = "user", key = "#request.login()")
    public UserDto register(RegisterUserRequest request) {

        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Пользователь с таким e-mail уже существует");
        }

        if (repository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("Пользователь с таким номером телефона уже существует");
        }

        if (repository.existsByLogin(request.login())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        String hash = passwordEncoder.encode(request.password());
        // У всех по умолчанию роль пользователя
        Role role = new Role(2L, "USER");

        User user = User.builder()
                .login(request.login())
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .passwordHash(hash)
                .role(role)
                .active(true)
                .build();
        repository.save(user);

        // Чтобы работал кэш, нигде дальше использоваться не должен
        return userMapper.toResponse(user);
    }

    public void login(LoginRequest request) {

        User user = repository.findByLogin(request.login())
                            .orElseThrow(() ->
                                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("Статус пользователя не активен");
        }
    }

    @Transactional
    @CachePut(value = "user", key = "#request.login()")
    public UserDto updateProfile(UpdateProfileRequest request) {

        User user = repository.findByLogin(request.login())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setAddress(request.address());

        return userMapper.toResponse(user);
    }

    @Cacheable(value = "user", key = "#login")
    public UserDto get(String login) {
        User user = repository.findByLogin(login)
                .orElseThrow();
        return userMapper.toResponse(user);
    }

    // Служебный запрос для поиска, кэш не нужен
    public UserDto getById(Long id) {
        User user = repository.findById(id)
                .orElseThrow();
        return userMapper.toResponse(user);
    }

    public String getRole(String login) {
        User user = repository.findByLogin(login)
                .orElseThrow();
        return user.getRole().getName();
    }

    public String getRole(Long id) {
        User user = repository.findById(id)
                .orElseThrow();
        return user.getRole().getName();
    }

    // Первый запрос о смене пароля
    @Transactional
    public void initiatePasswordReset(PasswordResetRequest request) {

        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        String rawToken = passwordChangeService.generateToken();
        String tokenHash = passwordChangeService.hashToken(rawToken);

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        user.getId(),
                        tokenHash,
                        LocalDateTime.now().plusHours(1)
                );

        tokenRepository.save(resetToken);

        // публикуем событие
        publisher.publishEvent(
                new PasswordResetRequestedEvent(
                        user.getEmail(),
                        rawToken
                )
        );
    }
    // Успешный ввод токена, смена пароля
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {

        String tokenHash = passwordChangeService.hashToken(request.token());

        PasswordResetToken resetToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Invalid token"));

        if (resetToken.isExpired()) {
            throw new IllegalStateException("Token expired");
        }

        if (resetToken.isUsed()) {
            throw new IllegalStateException("Token already used");
        }

        User user = repository.findById(resetToken.getUserId())
                .orElseThrow();

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        tokenRepository.invalidateAllForUser(user.getId());

        resetToken.markUsed();

        repository.save(user);
        tokenRepository.save(resetToken);
    }

    @Transactional
    @CachePut(value = "user", key = "#result.login()")
    public CompletableFuture<UserDto> uploadAvatar(Long userId, MultipartFile file) throws Exception {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        UUID oldAvatarId = user.getAvatarMediaId();

        // incidentId = null, значит аватар
        return mediaApi.upload(file, userId, null)
                .thenApply(dto -> {
                    // Если был старый аватар — удаляем ТОЛЬКО связь с incident_id = NULL
                    // Сам файл остаётся, если используется в инцидентах
                    if (oldAvatarId != null && !oldAvatarId.equals(dto.id())) {
                        try {
                            mediaApi.unlinkAvatar(oldAvatarId, userId);
                        } catch (Exception e) {
                            log.warn("Не удалось отвязать старый аватар {}: {}", oldAvatarId, e.getMessage());
                        }
                    }

                    user.setAvatarMediaId(dto.id());
                    repository.save(user);
                    return userMapper.toResponse(user);
                });
    }

    @Transactional
    @CachePut(value = "user", key = "#result.login()")
    public UserDto deleteAvatar(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (user.getAvatarMediaId() != null) {
            UUID avatarId = user.getAvatarMediaId();

            // Удаляем ссылку из данных пользователя
            user.setAvatarMediaId(null);
            repository.save(user);
            // Удаляем связь из таблицы, если связей больше нет, файл удалится
            try {
                mediaApi.unlinkAvatar(avatarId, userId);
            } catch (Exception e) {
                log.warn("Не удалось удалить связь аватара {}: {}", avatarId, e.getMessage());
            }
        }

        return userMapper.toResponse(user);
    }
}