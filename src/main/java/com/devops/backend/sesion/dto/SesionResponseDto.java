package com.devops.backend.sesion.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record SesionResponseDto(
        Long idSesion,
        Long idUsuario,
        String nombresUsuario,
        String apellidosUsuario,
        LocalDate fechaSesion,
        LocalTime horaSesion) {
}
