package com.devops.backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseAdmin(

        @Schema(description = "ID único del usuario", example = "123") Long idUsuario,

        @Schema(description = "Nombres del usuario", example = "Juan") String nombres,

        @Schema(description = "Apellidos del usuario", example = "Pérez García") String apellidos,

        @Schema(description = "Número de documento de identificación", example = "1234567890") String documento,

        @Schema(description = "Género del usuario", example = "masculino|femenino") String genero,

        @Schema(description = "Fecha de nacimiento del usuario", example = "1990-05-15") String fechaNacimiento,

        @Schema(description = "Número de teléfono del usuario", example = "+57 300 345 678") String telefono,

        @Schema(description = "Estado del usuario", example = "ACTIVO") String estado,

        @Schema(description = "Nombre del rol asignado al usuario", example = "ROLE_USER") String nombreRol,
        @Schema(description = "Fecha de creación del registro", example = "2026-05-01T10:30:00") String creadoEn,

        @Schema(description = "Fecha de última actualización del registro", example = "2026-05-01T15:45:00") String actualizadoEn


) {
}
