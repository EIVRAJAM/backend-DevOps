package com.devops.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El correo o username es obligatorio")
        String usernameOrEmail,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
