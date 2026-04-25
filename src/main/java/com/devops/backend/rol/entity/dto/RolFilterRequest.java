package com.devops.backend.rol.entity.dto;

public record RolFilterRequest(
    Long idRol,
    String nombreRol,
    String estado,
    int page,
    int size
    ) {
    public RolFilterRequest {
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
        }
    }
