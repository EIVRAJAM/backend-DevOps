package com.devops.backend.rol.entity.dto;

import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.rol.enums.Estado;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Información completa de un rol incluyendo sus funcionalidades asignadas")
public record RolResponse(

        @Schema(description = "ID único del rol", example = "1")
        Long idRol,

        @Schema(description = "Nombre del rol en formato ROLE_NOMBRE", example = "ROLE_ADMIN")
        String nombreRol,

        @Schema(description = "Estado actual del rol (ACTIVO o INACTIVO)", example = "ACTIVO")
        Estado estado,

        @Schema(description = "Fecha y hora de creación del rol (ISO-8601)", example = "2026-04-01T10:00:00")
        LocalDateTime creadoEn,

        @Schema(description = "Fecha y hora de la última actualización (ISO-8601)", example = "2026-04-18T15:30:00")
        LocalDateTime actualizadoEn,

        @Schema(description = "Lista de funcionalidades asignadas al rol")
        List<FuncionalidadResponse> funcionalidades

) {}