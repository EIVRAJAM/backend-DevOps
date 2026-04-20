package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoEvento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Record DTO para cambiar el estado de un evento
 */
@Schema(description = "Información para cambiar el estado de un evento (transición de estado)")
public record ChangeEstadoDTO(
        @NotNull(message = "El nuevo estado es requerido") 
        @Schema(description = "Nuevo estado del evento", example = "PUBLICADO", allowableValues = {"BORRADOR", "PUBLICADO", "CERRADO", "CANCELADO" }) 
        EstadoEvento nuevoEstado,

        @Size(max = 500, message = "El comentario no debe exceder 500 caracteres") 
        @Schema(description = "Comentario adicional sobre el cambio de estado (opcional)", example = "Evento listo para publicación") 
        String comentario
) {}
