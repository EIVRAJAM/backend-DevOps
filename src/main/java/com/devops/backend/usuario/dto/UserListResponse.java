package com.devops.backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información básica de un usuario del sistema")
public record UserListResponse(
        @Schema(description = "ID único del usuario", example = "123") Long idUsuario,

        @Schema(description = "Nombres del usuario", example = "Juan") String nombres,

        @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

        @Schema(description = "Número de documento de identificación", example = "1234567890") String documento,
        @Schema(description = "Número de documento de identificación", example = "1234567890") String genero,

        @Schema(description = "Fecha de nacimiento del usuario", example = "1990-05-15") String fechaNacimiento,

        @Schema(description = "Número de teléfono del usuario", example = "+57 3001234567") String telefono
        ) {
}
