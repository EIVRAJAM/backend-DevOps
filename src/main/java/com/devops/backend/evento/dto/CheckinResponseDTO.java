package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoTicket;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta tras realizar check-in exitoso")
public record CheckinResponseDTO(
        @Schema(description = "ID del ticket", example = "1")
        Long idTicket,
        
        @Schema(description = "ID del evento", example = "5")
        Long idEvento,
        
        @Schema(description = "Nombre del evento", example = "DevOps Summit")
        String nombreEvento,
        
        @Schema(description = "Nombre completo del asistente (Nombres Apellidos)", example = "Juan Perez")
        String nombreAsistente,
        
        @Schema(description = "Estado actual del ticket", example = "PAGADO")
        EstadoTicket estadoTicket,
        
        @Schema(description = "Indica si el check-in fue realizado", example = "true")
        Boolean checkinRealizado,
        
        @Schema(description = "Fecha y hora exacta del check-in")
        LocalDateTime fechaCheckin
) {}
