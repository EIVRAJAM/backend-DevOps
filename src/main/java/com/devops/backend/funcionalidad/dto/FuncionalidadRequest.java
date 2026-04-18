package com.devops.backend.funcionalidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FuncionalidadRequest(

        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombreFuncionalidad,

        @Size(max = 250, message = "La URL no puede superar 250 caracteres")
        String urlFuncionalidad,

        // null si es funcionalidad padre
        Long idPadre
) {}