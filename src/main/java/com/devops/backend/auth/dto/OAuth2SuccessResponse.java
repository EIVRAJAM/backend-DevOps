package com.devops.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Respuesta tras autenticación exitosa con OAuth2
 * Contiene JWT del sistema y datos del usuario autenticado
 */
public record OAuth2SuccessResponse(
        @Schema(description = "JWT del sistema (Bearer token)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String token,

        @Schema(description = "Nombre de usuario", example = "juan.perez") String username,

        @Schema(description = "Correo del usuario", example = "juan@gmail.com") String correo,

        @Schema(description = "Identificador del usuario", example = "123") Long userId,

        @Schema(description = "Roles asignados al usuario", example = "[\"ROLE_USER\"]") List<String> roles) {
}
