package com.devops.backend.funcionalidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FuncionalidadUpdateRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombreFuncionalidad,
        @Size(max = 250)
        String urlFuncionalidad,
        @NotBlank(message = "El estado es obligatorio")
        String estado,
        Long idPadre
) {}
