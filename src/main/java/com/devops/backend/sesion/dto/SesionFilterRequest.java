package com.devops.backend.sesion.dto;

import java.time.LocalDate;

public record SesionFilterRequest(
        Long idUsuario,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        int page,
        int size) {
}
