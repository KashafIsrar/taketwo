package com.taketwo.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String emailOrUsername,
        @NotBlank String password
) {}
