package com.devops.backend.rol.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Parámetros de filtrado y paginación para la consulta de roles")
public record RolFilterRequest(

        @Schema(description = "Filtro opcional: ID único del rol", example = "1")
        Long idRol,

        @Schema(description = "Filtro opcional: nombre del rol (búsqueda parcial)", example = "ADMIN")
        String nombreRol,

        @Schema(description = "Filtro opcional: estado del rol (ACTIVO o INACTIVO)", example = "ACTIVO")
        String estado,

        @Schema(description = "Número de página (comienza en 0)", example = "0")
        int page,

        @Schema(description = "Cantidad de registros por página", example = "10")
        int size

) {
    public RolFilterRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
    }
}