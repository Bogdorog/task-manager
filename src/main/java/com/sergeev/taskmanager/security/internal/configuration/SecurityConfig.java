package com.sergeev.taskmanager.security.internal.configuration;

import com.sergeev.taskmanager.security.internal.jwt.RefreshTokenAuthenticationFilter;
import com.sergeev.taskmanager.security.internal.login.LoginAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String SIGNIN_ENTRY_POINT = "/auth/login";
    public static final String SIGNUP_ENTRY_POINT = "/auth/register";
    public static final String SWAGGER_ENTRY_POINT = "/swagger-ui/**";
    public static final String FORGOT_PASSWORD_POINT = "/user/password/reset/request";
    public static final String RESET_PASSWORD_POINT = "/user/password/reset/confirm";
    public static final String API_DOCS_ENTRY_POINT = "/api-docs/**";
    public static final String TOKEN_REFRESH_ENTRY_POINT = "/auth/refreshToken";
    private final AuthenticationManager authenticationManager;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler failureHandler;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http,
                                    final JwtAuthenticationConverter jwtAuthenticationConverter) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SIGNIN_ENTRY_POINT).permitAll()
                        .requestMatchers(SIGNUP_ENTRY_POINT).permitAll()
                        .requestMatchers(SWAGGER_ENTRY_POINT).permitAll()
                        .requestMatchers(API_DOCS_ENTRY_POINT).permitAll()
                        .requestMatchers(TOKEN_REFRESH_ENTRY_POINT).permitAll()
                        .requestMatchers(FORGOT_PASSWORD_POINT).permitAll()
                        .requestMatchers(RESET_PASSWORD_POINT).permitAll()
                        .requestMatchers("/api/incidents/admin").hasRole("ADMIN")
                        .requestMatchers("/user/admin/userstats").hasRole("ADMIN")
                        .requestMatchers("/api/incidents/{incidentId}/photos/{mediaId}/force").hasRole("ADMIN")
                        .requestMatchers("/api/incidents/{incidentId}/photos").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(buildLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildRefreshTokenProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                );
        return http.build();
    }

    @Bean
    protected LoginAuthenticationFilter buildLoginProcessingFilter() {
        LoginAuthenticationFilter filter = new LoginAuthenticationFilter(SIGNIN_ENTRY_POINT,
                authenticationSuccessHandler, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }


    @Bean
    protected RefreshTokenAuthenticationFilter buildRefreshTokenProcessingFilter() {
        RefreshTokenAuthenticationFilter filter = new RefreshTokenAuthenticationFilter(TOKEN_REFRESH_ENTRY_POINT,
                authenticationSuccessHandler, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
