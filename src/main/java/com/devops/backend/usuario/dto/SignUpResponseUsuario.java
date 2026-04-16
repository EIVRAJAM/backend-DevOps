package com.devops.backend.usuario.dto;

public record SignUpResponseUsuario(
        Long idUsuario,
        String nombres,
        String apellidos,
        String telefono
) {
}
