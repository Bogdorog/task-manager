package com.sergeev.taskmanager.user.internal.service;

import com.sergeev.taskmanager.user.api.UserApi;
import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
class UserPublicApiImpl implements UserApi {

    private final UserService service;

    public void registerUser(RegisterUserRequest request) {
        service.register(request);
    }

    public UserDto getUser(String login) {
        return service.get(login);
    }

    public UserDto getUserById(Long id) { return service.getById(id); }

    public String getRole(String login) {return service.getRole(login);}

    public String getRole(Long id) {return service.getRole(id);}

    public void login(LoginRequest request) {
        service.login(request);
    }

    public void initiatePasswordReset(PasswordResetRequest request) {
        service.initiatePasswordReset(request);
    }

    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        service.confirmPasswordReset(request);
    }

    public UserDto updateProfile(UpdateProfileRequest request) {
        return service.updateProfile(request);
    }

    public CompletableFuture<UserDto> uploadAvatar(Long userId, MultipartFile file) throws Exception {
        return service.uploadAvatar(userId, file);
    }

    public UserDto deleteAvatar(Long userId)
    {
        return service.deleteAvatar(userId);
    }
}
