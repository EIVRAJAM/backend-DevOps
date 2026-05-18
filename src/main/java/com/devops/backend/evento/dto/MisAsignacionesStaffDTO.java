package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoEvento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Evento al que el usuario está asignado como staff")
public record MisAsignacionesStaffDTO(
        @Schema(description = "ID del evento", example = "5")
        Long idEvento,
        
        @Schema(description = "Nombre del evento", example = "DevOps Summit 2026")
        String nombreEvento,
        
        @Schema(description = "Fecha del evento")
        LocalDate fechaEvento,
        
        @Schema(description = "Hora del evento")
        LocalTime horaEvento,
        
        @Schema(description = "Lugar del evento")
        String lugarEvento,
        
        @Schema(description = "Estado actual del evento", example = "PUBLICADO")
        EstadoEvento estadoEvento
) {}
