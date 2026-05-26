package com.devops.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Información del usuario obtenida desde OAuth2 (Google)
 * Interno del sistema, no se devuelve al cliente
 */
public record OAuth2UserDTO(
        @Schema(description = "Email del usuario de Google", example = "juan@gmail.com") String email,

        @Schema(description = "Nombre del usuario de Google", example = "Juan") String nombre,

        @Schema(description = "Apellido del usuario de Google", example = "Pérez") String apellido,

        @Schema(description = "Proveedor OAuth2", example = "GOOGLE") String provider,

        @Schema(description = "URL de foto del usuario de Google") String photoUrl) {
}
