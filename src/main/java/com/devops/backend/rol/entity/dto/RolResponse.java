package com.devops.backend.rol.entity.dto;

import com.devops.backend.rol.enums.Estado;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;

import java.time.LocalDateTime;
import java.util.List;

public record RolResponse(
        Long idRol,
        String nombreRol,
        Estado estado,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,
        List<FuncionalidadResponse> funcionalidades
) {


}
