package com.sergeev.taskmanager.security.internal.login;

import com.sergeev.taskmanager.security.api.exception.AuthMethodNotSupportedException;
import com.sergeev.taskmanager.security.internal.utils.JsonUtils;
import com.sergeev.taskmanager.user.api.dto.request.LoginRequest;
import io.github.resilience4j.core.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Slf4j
public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;

    public LoginAuthenticationFilter(final String defaultFilterProcessesUrl,
                                     final AuthenticationSuccessHandler successHandler,
                                     final AuthenticationFailureHandler failureHandler) {
        super(defaultFilterProcessesUrl);
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Override
    public Authentication attemptAuthentication(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response) throws AuthenticationException {

        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            log.debug("Метод аутентификации не поддерживается. Запрос: {}", request.getMethod());
            throw new AuthMethodNotSupportedException("Метод аутентификации не поддерживается");
        }

        LoginRequest loginRequest;
        try {
            loginRequest = JsonUtils.fromReader(request.getReader(), LoginRequest.class);
        } catch (Exception e) {
            throw new AuthenticationServiceException("Invalid login request payload");
        }

        if (StringUtils.isBlank(loginRequest.login()) || StringUtils.isBlank(loginRequest.password())) {
            throw new AuthenticationServiceException("Отсутствует логин или пароль");
        }

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginRequest.login(), loginRequest.password());
        token.setDetails(authenticationDetailsSource.buildDetails(request));

        return getAuthenticationManager().authenticate(token);
    }

    @Override
    protected void successfulAuthentication(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain chain,
            @NonNull final Authentication authResult) throws IOException, ServletException {
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final AuthenticationException failed) throws IOException, ServletException {
        failureHandler.onAuthenticationFailure(request, response, failed);
    }
}
