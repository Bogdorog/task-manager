package com.sergeev.taskmanager.user.internal.controller;

import com.sergeev.taskmanager.user.api.UserApi;
import com.sergeev.taskmanager.user.api.dto.request.LoginRequest;
import com.sergeev.taskmanager.user.api.dto.request.RegisterUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Авторизация и Регистрация")
public class AuthController {

    private final UserApi userApi;

    public AuthController(UserApi userApi) {
        this.userApi = userApi;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя")
    @ApiResponse(
            responseCode = "200",
            description = "Пользователь успешно зарегистрирован."
    )
    @ApiResponse(
            responseCode = "500",
            description = "Ошибка на сервере."
    )
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request) {
        userApi.registerUser(request);
        return ResponseEntity.ok("Пользователь успешно зарегистрирован.");
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему и получение JWT-токена")
    @ApiResponse(
            responseCode = "200",
            description = "Вход прошел успешно.",
            content = @Content(mediaType = "application/json")
    )
    @ApiResponse(
            responseCode = "500",
            description = "Ошибка на сервере."
    )
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        userApi.login(request);
        return ResponseEntity.ok("");
    }
}
