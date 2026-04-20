package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoEvento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Record DTO para respuesta de historial de evento (lectura)
 */
@Schema(description = "Registro de cambio de estado de un evento")
public record HistorialEventoDTO(
        @Schema(description = "ID único del registro de historial", example = "1") 
        Long idHistorialEvento,

        @Schema(description = "Estado anterior del evento", example = "BORRADOR", allowableValues = {
                "BORRADOR", "PUBLICADO", "CERRADO", "CANCELADO" }) 
        EstadoEvento estadoAnterior,

        @Schema(description = "Nuevo estado del evento", example = "PUBLICADO", allowableValues = {
                "BORRADOR", "PUBLICADO", "CERRADO", "CANCELADO" }) 
        EstadoEvento estadoNuevo,

        @Schema(description = "Comentario adicional sobre el cambio de estado", example = "Evento listo para publicación") 
        String comentario,

        @Schema(description = "ID del usuario que realizó el cambio", example = "1") 
        Long idUsuarioResponsable,

        @Schema(description = "Nombre del usuario que realizó el cambio", example = "Juan Pérez") 
        String nombreUsuarioResponsable,

        @Schema(description = "Fecha y hora del cambio de estado (ISO-8601)", example = "2026-04-20T15:30:00") 
        LocalDateTime fechaCambio) {
}
