package com.devops.backend.rol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

@Schema(description = "Solicitud para asignar funcionalidades a un rol")
public record AsignarFuncionalidadesRequest(

        @NotEmpty(message = "Debe proporcionar al menos una funcionalidad")
        @Schema(
                description = "Conjunto de IDs de funcionalidades a asignar al rol (no puede estar vacío)",
                example = "[1, 2, 5, 8]"
        )
        Set<Long> idsFuncionalidades

) {}