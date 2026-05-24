package com.devops.backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informacion basica de usuario para busquedas del organizador")
public record UsuarioOrganizadorDTO(
        @Schema(description = "ID unico del usuario", example = "123")
        Long idUsuario,

        @Schema(description = "Nombres del usuario", example = "Juan")
        String nombres,

        @Schema(description = "Apellidos del usuario", example = "Perez Garcia")
        String apellidos,

        @Schema(description = "Numero de documento", example = "1234567890")
        String documento,

        @Schema(description = "Nombre de usuario (login)", example = "juan.perez")
        String username,

        @Schema(description = "Correo electronico", example = "juan@example.com")
        String correo,

        @Schema(description = "Telefono", example = "+57 3001234567")
        String telefono,

        @Schema(description = "Nombre del rol", example = "ROLE_USER")
        String nombreRol
) {}
