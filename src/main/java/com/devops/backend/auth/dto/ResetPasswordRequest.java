package com.devops.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResetPasswordRequest(
                @NotBlank(message = "El correo electrónico no puede estar vacío") @Email(message = "El correo electrónico debe ser válido") @Schema(description = "Correo electrónico asociado a la cuenta", example = "jperez@example.com") String email,

                @NotBlank(message = "El código no puede estar vacío") @Pattern(regexp = "^\\d{6}$", message = "El código debe ser exactamente 6 dígitos") @Schema(description = "Código de verificación de 6 dígitos enviado al correo", example = "123456") String code,

                @NotBlank(message = "La nueva contraseña no puede estar vacía") @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres") @Schema(description = "Nueva contraseña (8-128 caracteres, debe incluir mayúsculas, minúsculas y números)", example = "NewSecurePass456") String newPassword,

                @NotBlank(message = "La confirmación de contraseña no puede estar vacía") @Size(min = 8, max = 128, message = "La confirmación de contraseña debe tener entre 8 y 128 caracteres") @Schema(description = "Confirmación de la nueva contraseña (debe ser igual a newPassword)", example = "NewSecurePass456") String confirmPassword) {
}
