package com.sergeev.taskmanager.security.internal.utils;

import com.sergeev.taskmanager.exception.AuthMethodNotSupportedException;
import com.sergeev.taskmanager.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException exception) throws IOException {

        log.debug("Ошибка аутентификации: {}", exception.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                resolveMessage(exception),
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), error);
    }

    private String resolveMessage(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return "Неверный логин или пароль";
        }
        if (exception instanceof DisabledException) {
            return "Аккаунт отключен";
        }
        if (exception instanceof LockedException) {
            return "Аккаунт заблокирован";
        }
        if (exception instanceof AuthMethodNotSupportedException) {
            return "Метод аутентификации не поддерживается";
        }
        return "Ошибка аутентификации";
    }
}
