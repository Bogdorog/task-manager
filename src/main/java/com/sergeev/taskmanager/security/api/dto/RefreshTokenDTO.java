package com.sergeev.taskmanager.security.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDTO(
    @NotBlank
    @Schema(example = "refreshToken", description = "Refresh token")
    String refreshToken){}
