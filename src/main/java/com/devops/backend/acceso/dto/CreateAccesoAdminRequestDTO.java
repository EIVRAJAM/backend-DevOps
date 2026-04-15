package com.devops.backend.acceso.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccesoAdminRequestDTO(
        @NotNull(message = "El ID del usuario no puede estar vacío") Long idUsuario,

        @NotBlank(message = "El username no puede estar vacío") @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres") String username,

        @NotBlank(message = "El correo no puede estar vacío") @Email(message = "El correo debe ser válido") String correoAcceso) {
}
