package com.devops.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UnlockAccountRequest(
        @NotBlank(message = "El correo electrónico no puede estar vacío") @Email(message = "El correo electrónico debe ser válido") String email,

        @NotBlank(message = "El código de verificación no puede estar vacío") @Pattern(regexp = "^\\d{6}$", message = "El código debe ser exactamente 6 dígitos") String code) {
}
