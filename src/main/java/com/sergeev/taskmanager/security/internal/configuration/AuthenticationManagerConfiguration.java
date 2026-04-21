package com.sergeev.taskmanager.security.internal.configuration;

import com.sergeev.taskmanager.security.internal.jwt.RefreshTokenAuthenticationProvider;
import com.sergeev.taskmanager.security.internal.login.LoginAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

@Configuration
@RequiredArgsConstructor
public class AuthenticationManagerConfiguration {

    private final LoginAuthenticationProvider loginAuthenticationProvider;
    private final RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider;

    @Bean
    public AuthenticationManager authenticationManager(
            ObjectPostProcessor<Object> objectPostProcessor) throws Exception {
        AuthenticationManagerBuilder auth =
                new AuthenticationManagerBuilder(objectPostProcessor);
        auth.authenticationProvider(loginAuthenticationProvider);
        auth.authenticationProvider(refreshTokenAuthenticationProvider);
        return auth.build();
    }
}
