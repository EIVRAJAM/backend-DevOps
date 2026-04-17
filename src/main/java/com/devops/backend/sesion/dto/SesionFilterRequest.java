package com.devops.backend.sesion.dto;

import java.time.LocalDateTime;

public record SesionFilterRequest(
        Long idUsuario,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Boolean activa,
        int page,
        int size) {
}
