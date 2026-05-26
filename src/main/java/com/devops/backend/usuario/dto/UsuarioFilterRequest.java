package com.devops.backend.usuario.dto;

public record UsuarioFilterRequest(
        String documento,
        String nombre,
        String apellido,
        String nombreRol,
        int page,
        int size
) {}