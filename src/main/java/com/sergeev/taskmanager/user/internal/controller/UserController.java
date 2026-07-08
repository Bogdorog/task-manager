package com.sergeev.taskmanager.user.internal.controller;

import com.sergeev.taskmanager.user.api.dto.UserDto;
import com.sergeev.taskmanager.user.api.dto.request.ChangePasswordRequest;
import com.sergeev.taskmanager.user.api.dto.request.UpdateProfileRequest;
import com.sergeev.taskmanager.user.internal.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    public UserDto get(@Parameter(description = "Логин пользователя") @PathVariable String login) {
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

    @Operation(summary = "Изменить свой аватар")
    @PutMapping(value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto uploadAvatar(@RequestPart("file") MultipartFile file) {
        return service.uploadAvatar(file);
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Удалить свой аватар")
    public UserDto deleteAvatar() {
        return service.deleteAvatar();
    }

    @DeleteMapping("/me")
    @Operation(summary = "Запрос на удаление своего аккаунта")
    public void deleteRequest() {
        service.requestAccountDeletion();
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Смена пароля в профиле")
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        service.changePassword(request);
    }
}
