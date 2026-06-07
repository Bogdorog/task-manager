package com.sergeev.taskmanager.user.internal.controller;

import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.request.PasswordResetConfirmRequest;
import com.sergeev.taskmanager.user.api.dto.request.PasswordResetRequest;
import com.sergeev.taskmanager.user.api.dto.request.UpdateProfileRequest;
import com.sergeev.taskmanager.user.internal.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Управление профилем пользователя")
class UserController {

    private final UserService service;

    @GetMapping("/me")
    @Operation(summary = "Получить информацию о себе")
    public UserDto getMe() {
        return service.getMe();
    }

    @GetMapping("/{login}")
    @Operation(summary = "Получить информацию о другом пользователе")
    public UserDto get(@PathVariable String login) {
        return service.getUser(login);
    }

    @GetMapping("/{login}/role")
    @Operation(summary = "Служебный запрос на получение роли")
    public String getRole(@PathVariable String login) {
        return service.getRole(login);
    }

    @PutMapping("/me")
    @Operation(summary = "Изменить информацию о себе")
    public UserDto update(@RequestBody UpdateProfileRequest request) {
        return service.updateProfile(request);
    }

    @PostMapping("/password/reset/request")
    @Operation(summary = "Запрос на смену пароля")
    public void requestReset(@RequestBody PasswordResetRequest request) {
        service.initiatePasswordReset(request);
    }

    @PostMapping("/password/reset/confirm")
    @Operation(summary = "Смена пароля")
    public void confirmReset(@RequestBody PasswordResetConfirmRequest request) {
        service.confirmPasswordReset(request);
    }

    @Operation(summary = "Изменить свой аватар")
    @PutMapping(
            value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UserDto uploadAvatar(
            @RequestPart("file") MultipartFile file
    ) {

        return service.uploadAvatar(file);
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Удалить свой аватар")
    public UserDto deleteAvatar() {
        return service.deleteAvatar();
    }
}
