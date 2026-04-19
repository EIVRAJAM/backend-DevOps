package com.devops.backend.funcionalidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para crear una nueva funcionalidad")
public record FuncionalidadRequest(

                @NotBlank(message = "El nombre no puede estar vacío") @Size(max = 150, message = "El nombre no puede superar 150 caracteres") @Schema(description = "Nombre de la funcionalidad (será visible en menús)", example = "Gestión de Usuarios") String nombreFuncionalidad,

                @Size(max = 250, message = "La URL no puede superar 250 caracteres") @Schema(description = "URL o ruta de la funcionalidad en el frontend", example = "/admin/usuarios") String urlFuncionalidad,

                @Schema(description = "ID de la funcionalidad padre (null si es raíz/sin padre)", example = "null") Long idPadre) {
}