package com.devops.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "El correo electrónico no puede estar vacío") 
        @Email(message = "El correo electrónico debe ser válido") 
        String email
    ) {
}
