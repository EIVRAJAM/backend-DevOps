package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.Estado;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información de un usuario asignado como staff")
public record StaffResponseDTO(
        @Schema(description = "ID de la asignación", example = "1")
        Long idEventoStaff,
        
        @Schema(description = "ID del evento", example = "5")
        Long idEvento,
        
        @Schema(description = "ID del usuario asignado", example = "12")
        Long idUsuario,
        
        @Schema(description = "Nombre completo del usuario (Nombres Apellidos)", example = "Juan Perez")
        String nombreCompleto,
        
        @Schema(description = "Estado de la asignación", example = "ACTIVO")
        Estado estado,
        
        @Schema(description = "Fecha de asignación")
        LocalDateTime asignadoEn
) {}
