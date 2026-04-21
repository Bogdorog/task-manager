package com.sergeev.taskmanager.security.internal.jwt;

import com.sergeev.taskmanager.security.api.dto.RefreshTokenDTO;
import com.sergeev.taskmanager.security.api.exception.AuthMethodNotSupportedException;
import com.sergeev.taskmanager.security.internal.utils.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Slf4j
public class RefreshTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationSuccessHandler successHandler;

    private final AuthenticationFailureHandler failureHandler;

    public RefreshTokenAuthenticationFilter(final String url,
                                            final AuthenticationSuccessHandler successHandler,
                                            final AuthenticationFailureHandler failureHandler) {
        super(url);
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) throws AuthenticationException {
        validateRequest(request);
        RefreshTokenDTO refreshTokenDto = getRefreshTokenDTO(request);
        validateRefreshToken(refreshTokenDto);
        return getAuthenticationManager().authenticate(new RefreshJwtAuthenticationToken(refreshTokenDto.refreshToken()));
    }

    private void validateRequest(final HttpServletRequest request) {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            log.debug("Метод аутентификации не поддерживается. Запрос: {}", request.getMethod());
            throw new AuthMethodNotSupportedException("Метод аутентификации не поддерживается");
        }
    }

    private static void validateRefreshToken(final RefreshTokenDTO refreshTokenDto) {
        if (StringUtils.isBlank(refreshTokenDto.refreshToken())) {
            throw new AuthenticationServiceException("Отсутствует логин или пароль");
        }
    }

    private static RefreshTokenDTO getRefreshTokenDTO(final HttpServletRequest request) {
        RefreshTokenDTO refreshTokenDto;
        try {
            refreshTokenDto = JsonUtils.fromReader(request.getReader(), RefreshTokenDTO.class);
        } catch (Exception e) {
            throw new AuthenticationServiceException("Invalid email request payload");
        }
        return refreshTokenDto;
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final FilterChain chain,
                                            final Authentication authResult) throws IOException, ServletException {
        this.successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final AuthenticationException failed) throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }
}

