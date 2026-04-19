package com.devops.backend.funcionalidad.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con información completa de una funcionalidad")
public record FuncionalidadResponse(
                @Schema(description = "ID único de la funcionalidad", example = "1") Long idFuncionalidad,

                @Schema(description = "Nombre de la funcionalidad", example = "Gestión de Usuarios") String nombreFuncionalidad,

                @Schema(description = "URL/ruta en el frontend", example = "/admin/usuarios") String urlFuncionalidad,

                @Schema(description = "Estado (ACTIVA o INACTIVA)", example = "ACTIVA") String estado,

                @Schema(description = "ID de la funcionalidad padre (null si es raíz)", example = "null") Long idPadre) {
}