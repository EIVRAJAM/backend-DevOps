package com.devops.backend.evento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "Estado actual del check-in de un evento")
public record EstadoCheckinDTO(
        @Schema(description = "Indica si el check-in esta habilitado en este momento", example = "true")
        boolean habilitado,

        @Schema(description = "Motivo del estado actual", example = "Check-in disponible")
        String motivo,

        @Schema(description = "ID del evento", example = "5")
        Long eventoId,

        @Schema(description = "Nombre del evento", example = "Conferencia DevOps")
        String nombreEvento,

        @Schema(description = "Estado del evento (PUBLICADO, BORRADOR, CERRADO, CANCELADO)", example = "PUBLICADO")
        String estadoEvento,

        @Schema(description = "Estado general del registro (ACTIVO, INACTIVO)", example = "ACTIVO")
        String estado,

        @Schema(description = "Fecha programada del evento")
        LocalDate fechaEvento,

        @Schema(description = "Hora programada del evento")
        LocalTime horaEvento,

        @Schema(description = "Fecha y hora de apertura del check-in (1 h antes del evento)")
        LocalDateTime aperturaCheckin,

        @Schema(description = "Fecha y hora actual en zona America/Bogota")
        LocalDateTime ahora
) {}
