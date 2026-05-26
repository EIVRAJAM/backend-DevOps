package com.devops.backend.evento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Petición para asignar un usuario como staff de un evento")
public record AsignarStaffRequestDTO(
        @Schema(description = "ID del usuario a asignar", example = "12", required = true)
        @NotNull(message = "El ID del usuario es requerido")
        Long idUsuario
) {}
