package com.devops.backend.usuario.dto;

public record UserListResponse(Long   idUsuario,
                               String nombres,
                               String apellidos,
                               String documento,
                               String nombreRol) {
}
