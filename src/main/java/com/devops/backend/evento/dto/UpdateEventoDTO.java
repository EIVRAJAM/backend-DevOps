package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.Moneda;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Record DTO para actualizar un evento existente (solo contenido)
 * Los cambios de estado se realizan a través de endpoints PATCH específicos
 */
@Schema(description = "Información para actualizar un evento existente (solo contenido, todos los campos son opcionales)")
public record UpdateEventoDTO(
        @Size(min = 1, max = 150, message = "El nombre debe tener entre 1 y 150 caracteres") 
        @Schema(description = "Nuevo nombre del evento (opcional)", example = "Conferencia de DevOps 2026 - Edición Especial") 
        String nombreEvento,

        @Size(max = 5000, message = "La descripción no debe exceder 5000 caracteres") 
        @Schema(description = "Nueva descripción del evento (opcional)", example = "Conferencia actualizada con nuevas temáticas") 
        String descripcionEvento,

        @FutureOrPresent(message = "La fecha del evento debe ser hoy o en el futuro") 
        @Schema(description = "Nueva fecha del evento (opcional)", example = "2026-05-20") 
        LocalDate fechaEvento,

        @Schema(description = "Nueva hora del evento (opcional)", example = "11:00:00") 
        LocalTime horaEvento,

        @Size(min = 1, max = 200, message = "El lugar debe tener entre 1 y 200 caracteres") 
        @Schema(description = "Nuevo lugar del evento (opcional)", example = "Auditorio Principal Bogotá") 
        String lugarEvento,

        @Size(max = 255, message = "La referencia de ubicación no debe exceder 255 caracteres") 
        @Schema(description = "Nueva referencia de ubicación (opcional)", example = "Pabellón B, Nivel 3") 
        String referenciaUbicacion,

        @Size(max = 500, message = "La URL de imagen no debe exceder 500 caracteres") 
        @Schema(description = "Nueva URL de imagen (opcional)", example = "https://example.com/evento-banner-2.jpg") 
        String imagenUrl,

        @Min(value = 0, message = "La capacidad máxima no puede ser negativa")
        @Schema(description = "Nueva capacidad máxima (opcional)", example = "600") 
        Integer capacidadMaxima,

        @Schema(description = "Indica si tendrá parqueadero (opcional)", example = "true") 
        Boolean tieneParqueadero,

        @Min(value = 0, message = "Los cupos de parqueadero no pueden ser negativos") 
        @Schema(description = "Nuevos cupos de parqueadero (opcional)", example = "150") 
        Integer cuposParqueadero,

        @Schema(description = "Indica si será evento de pago (opcional). Si cambia a false, no envíe precio ni moneda", example = "false") 
        Boolean esDePago,

        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0") 
        @Schema(description = "Nuevo precio del evento (obligatorio solo si esDePago=true). No envíe si es gratuito", nullable = true) 
        BigDecimal precio,

        @Schema(description = "Nueva moneda (obligatorio solo si esDePago=true). USD, COP, EUR, MXN. No envíe si es gratuito", nullable = true,
        allowableValues = {"USD", "COP", "EUR", "MXN" }) 
        Moneda moneda
) {}
