package com.devops.backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de creación exitosa de un nuevo usuario")
public record SignUpResponseUsuario(
                @Schema(description = "ID único del usuario creado", example = "123") Long idUsuario,

                @Schema(description = "Número de documento de identidad", example = "1234567890") String documento,

                @Schema(description = "Nombres del usuario", example = "Juan") String nombres,

                @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

                @Schema(description = "Número de teléfono de contacto", example = "+57 3001234567") String telefono,

                @Schema(description = "Nombre del rol del usuario", example = "ADMIN") String nombreRol
) {
}
