package com.devops.backend.evento.dto;

import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Record DTO para transmitir un comentario en acciones de evento
 * Utilizado en acciones que requieren contexto como cancelar o desactivar
 */
@Schema(description = "DTO para incluir comentario/motivo en acciones de evento")
public record ComentarioRequest(
        @Size(max = 500, message = "El comentario no debe exceder 500 caracteres") 
        @Schema(description = "Motivo o comentario adicional sobre la acción", example = "Evento cancelado por fuerza mayor") 
        String comentario) {
}
