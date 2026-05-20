package com.sergeev.taskmanager.user.api;

import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.request.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface UserApi {

    UserDto register(RegisterUserRequest request);

    UserDto login(LoginRequest request);

    void initiatePasswordReset(PasswordResetRequest request);

    void confirmPasswordReset(PasswordResetConfirmRequest request);

    UserDto updateProfile(UpdateProfileRequest request);

    UserDto getUser(String login);

    UserDto getUserById(Long id);

    UserDto getMe();

    String getRole(String login);

    String getRole(Long id);

    CompletableFuture<UserDto> uploadAvatar(MultipartFile file) throws Exception;

    UserDto deleteAvatar();
}
