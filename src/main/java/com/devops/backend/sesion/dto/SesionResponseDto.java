package com.devops.backend.sesion.dto;

import java.time.LocalDateTime;

public record SesionResponseDto(
        Long idSesion,
        Long idUsuario,
        String nombresUsuario,
        String apellidosUsuario,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Boolean activa,
        String tokenJti) {
}
