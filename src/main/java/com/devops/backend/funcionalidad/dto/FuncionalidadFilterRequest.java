package com.devops.backend.funcionalidad.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Criterios de filtro para búsqueda de funcionalidades")
public record FuncionalidadFilterRequest(
                @Schema(description = "Filtro por estado: ACTIVA o INACTIVA (opcional)", example = "ACTIVA") String estado,

                @Schema(description = "Filtro por ID de funcionalidad padre para ver solo sus hijas. null = solo raíces (opcional)", example = "null") Long id_padre) {
}
