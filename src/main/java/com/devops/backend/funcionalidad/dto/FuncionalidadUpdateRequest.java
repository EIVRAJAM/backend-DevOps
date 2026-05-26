package com.devops.backend.funcionalidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para actualizar una funcionalidad existente")
public record FuncionalidadUpdateRequest(

                @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) @Schema(description = "Nuevo nombre de la funcionalidad", example = "Gestión de Usuarios") String nombreFuncionalidad,

                @Size(max = 250) @Schema(description = "Nueva URL/ruta en el frontend", example = "/admin/usuarios") String urlFuncionalidad,

                @NotBlank(message = "El estado es obligatorio") @Schema(description = "Nuevo estado (ACTIVA o INACTIVA)", example = "ACTIVA") String estado,

                @Schema(description = "Nuevo ID de funcionalidad padre (null si debe ser raíz)", example = "null") Long idPadre) {
}
