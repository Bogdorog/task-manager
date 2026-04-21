package com.sergeev.taskmanager.user.internal.controller;

import com.sergeev.taskmanager.user.api.UserApi;
import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.request.PasswordResetConfirmRequest;
import com.sergeev.taskmanager.user.api.dto.request.PasswordResetRequest;
import com.sergeev.taskmanager.user.api.dto.request.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
class UserController {

    private final UserApi api;

    @GetMapping("/{login}")
    public Object get(@PathVariable String login) {
        return api.getUser(login);
    }

    @GetMapping("/{login}/role")
    public String getRole(@PathVariable String login) {
        return api.getRole(login);
    }

    @PostMapping("/{login}")
    public Object update(@RequestBody UpdateProfileRequest request) {
        return api.updateProfile(request);
    }

    @PostMapping("/password/reset/request")
    public void requestReset(@RequestBody PasswordResetRequest request) {
        api.initiatePasswordReset(request);
    }

    @PostMapping("/password/reset/confirm")
    public void confirmReset(@RequestBody PasswordResetConfirmRequest request) {
        api.confirmPasswordReset(request);
    }

    @PutMapping("/{login}/avatar")
    public CompletableFuture<UserDto> uploadAvatar(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        return api.uploadAvatar(userId, file);
    }

    @DeleteMapping("/{login}/avatar")
    public UserDto deleteAvatar(@RequestHeader("X-User-Id") Long userId) {
        return api.deleteAvatar(userId);
    }
}
