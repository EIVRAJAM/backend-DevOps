package com.devops.backend.rol.entity.dto;

import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;

import java.time.LocalDateTime;
import java.util.Set;

public record RolResponse(
        Long idRol,
        String nombreRol,
        String estado,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,
        Set<FuncionalidadResponse> funcionalidades
) {


}
