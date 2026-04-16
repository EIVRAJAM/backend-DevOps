package com.devops.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "El correo electrónico no puede estar vacío") 
        @Email(message = "El correo electrónico debe ser válido") 
        String email,

        @NotBlank(message = "El código no puede estar vacío") 
        @Pattern(regexp = "^\\d{6}$", message = "El código debe ser exactamente 6 dígitos") 
        String code,

        @NotBlank(message = "La nueva contraseña no puede estar vacía") 
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres") 
        String newPassword,

        @NotBlank(message = "La confirmación de contraseña no puede estar vacía") 
        @Size(min = 8, max = 128, message = "La confirmación de contraseña debe tener entre 8 y 128 caracteres") 
        String confirmPassword) {
}
