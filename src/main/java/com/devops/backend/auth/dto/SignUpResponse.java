package com.devops.backend.auth.dto;

public record SignUpResponse(
        Long idUsuario,
        String nombres,
        String apellidos,
        String correo
) {
}
