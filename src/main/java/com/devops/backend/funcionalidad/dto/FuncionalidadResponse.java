package com.devops.backend.funcionalidad.dto;


public record FuncionalidadResponse(
        Long idFuncionalidad,
        String nombreFuncionalidad,
        String urlFuncionalidad,
        String estado,
        Long idPadre  // null si es raíz
) {}