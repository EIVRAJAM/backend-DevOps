package com.devops.backend.acceso.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para crear una nueva cuenta de acceso administrativo a un usuario existente")
public record CreateAccesoAdminRequestDTO(
                @NotNull(message = "El ID del usuario no puede estar vacío") @Schema(description = "ID único del usuario existente al que se le creará acceso", example = "123") Long idUsuario,

                @NotBlank(message = "El username no puede estar vacío") @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres") @Schema(description = "Nombre de usuario único para acceso al sistema", example = "jperez") String username,

                @NotBlank(message = "El correo no puede estar vacío") @Email(message = "El correo debe ser válido") @Schema(description = "Correo electrónico único para recepciones y recuperación", example = "jperez@example.com") String correoAcceso) {
}
