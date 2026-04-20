package com.devops.backend.evento.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Record DTO para crear un nuevo evento
 */
@Schema(description = "Información requerida para crear un nuevo evento")
public record CreateEventoDTO(
        @NotBlank(message = "El nombre del evento es requerido") 
        @Size(min = 1, max = 150, message = "El nombre debe tener entre 1 y 150 caracteres") 
        @Schema(description = "Nombre descriptivo del evento", example = "Conferencia de DevOps 2026") 
        String nombreEvento,

        @Size(max = 5000, message = "La descripción no debe exceder 5000 caracteres") 
        @Schema(description = "Descripción detallada del evento (opcional)", example = "Una conferencia dedicada a prácticas DevOps modernas") 
        String descripcionEvento,

        @NotNull(message = "La fecha del evento es requerida") 
        @FutureOrPresent(message = "La fecha del evento debe ser hoy o en el futuro") 
        @Schema(description = "Fecha del evento en formato ISO-8601", example = "2026-05-15") 
        LocalDate fechaEvento,

        @NotNull(message = "La hora del evento es requerida") 
        @Schema(description = "Hora del evento en formato ISO-8601", example = "10:30:00") 
        LocalTime horaEvento,

        @NotBlank(message = "El lugar del evento es requerido") 
        @Size(min = 1, max = 200, message = "El lugar debe tener entre 1 y 200 caracteres") 
        @Schema(description = "Ubicación física del evento", example = "Centro de Convenciones Bogotá") 
        String lugarEvento,

        @Size(max = 255, message = "La referencia de ubicación no debe exceder 255 caracteres") 
        @Schema(description = "Referencia adicional de ubicación (salón, piso, etc.)", example = "Pabellón A, Nivel 2") 
        String referenciaUbicacion,

        @Size(max = 500, message = "La URL de imagen no debe exceder 500 caracteres") 
        @Schema(description = "URL de la imagen promocional del evento", example = "https://example.com/evento-banner.jpg") 
        String imagenUrl,

        @NotNull(message = "La capacidad máxima es requerida")
        @Min(value = 0, message = "La capacidad máxima no puede ser negativa")
        @Schema(description = "Capacidad máxima de asistentes", example = "500")
        Integer capacidadMaxima,

        @NotNull(message = "Debe indicar si tiene parqueadero")
        @Schema(description = "Indica si el evento cuenta con servicio de parqueadero", example = "true")
        Boolean tieneParqueadero,

        @Min(value = 0, message = "Los cupos de parqueadero no pueden ser negativos")
        @Schema(description = "Número de cupos de parqueadero disponibles (solo si tieneParqueadero=true)", example = "100")
        Integer cuposParqueadero) {
}
