package com.devops.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

public record UnlockAccountRequest(
                @NotBlank(message = "El correo electrónico no puede estar vacío") @Email(message = "El correo electrónico debe ser válido") @Schema(description = "Correo electrónico de la cuenta bloqueada", example = "jperez@example.com") String email,

                @NotBlank(message = "El código de verificación no puede estar vacío") @Pattern(regexp = "^\\d{6}$", message = "El código debe ser exactamente 6 dígitos") @Schema(description = "Código de desbloqueo de 6 dígitos enviado al correo", example = "654321") String code) {
}
