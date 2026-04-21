package com.sergeev.taskmanager.user.api;

import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.request.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface UserApi {

    void registerUser(RegisterUserRequest request);

    void login(LoginRequest request);

    void initiatePasswordReset(PasswordResetRequest request);

    void confirmPasswordReset(PasswordResetConfirmRequest request);

    UserDto updateProfile(UpdateProfileRequest request);

    UserDto getUser(String login);

    UserDto getUserById(Long id);

    String getRole(String login);

    String getRole(Long id);

    CompletableFuture<UserDto> uploadAvatar(Long userId, MultipartFile file) throws Exception;

    UserDto deleteAvatar(Long userId);
}
