package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Record DTO para respuesta de evento (lectura)
 */
@Schema(description = "Información completa de un evento registrado en el sistema")
public record EventoResponseDTO(
        @Schema(description = "ID único del evento", example = "1") 
        Long idEvento,

        @Schema(description = "ID del usuario que creó el evento", example = "1") 
        Long idUsuarioCreador,

        @Schema(description = "Nombre del evento", example = "Conferencia de DevOps 2026") 
        String nombreEvento,

        @Schema(description = "Descripción detallada del evento", example = "Una conferencia dedicada a prácticas DevOps") 
        String descripcionEvento,

        @Schema(description = "Fecha del evento", example = "2026-05-15") 
        LocalDate fechaEvento,

        @Schema(description = "Hora del evento", example = "10:30:00") 
        LocalTime horaEvento,

        @Schema(description = "Ubicación física del evento", example = "Centro de Convenciones Bogotá") 
        String lugarEvento,

        @Schema(description = "Referencia adicional de ubicación", example = "Pabellón A, Nivel 2") 
        String referenciaUbicacion,

        @Schema(description = "URL de la imagen promocional", example = "https://example.com/evento-banner.jpg") 
        String imagenUrl,

        @Schema(description = "Estado del evento", example = "PUBLICADO", allowableValues = {
                "BORRADOR", "PUBLICADO", "CERRADO", "CANCELADO" }
        ) 
        EstadoEvento estadoEvento,

        @Schema(description = "Capacidad máxima de asistentes", example = "500") 
        Integer capacidadMaxima,

        @Schema(description = "Indica si tiene parqueadero", example = "true") 
        Boolean tieneParqueadero,

        @Schema(description = "Cupos de parqueadero disponibles", example = "100") 
        Integer cuposParqueadero,

        @Schema(description = "Estado del registro (ACTIVO/INACTIVO)", example = "ACTIVO", allowableValues = {
                "ACTIVO",
                "INACTIVO" }) 
        Estado estado,

        @Schema(description = "Fecha de creación del registro (ISO-8601)", example = "2026-04-20T10:00:00") 
        LocalDateTime creadoEn,

        @Schema(description = "Fecha de última actualización (ISO-8601)", example = "2026-04-20T15:30:00") 
        LocalDateTime actualizadoEn) {
}
