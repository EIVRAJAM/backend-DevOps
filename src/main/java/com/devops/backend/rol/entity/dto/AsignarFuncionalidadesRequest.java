package com.devops.backend.rol.entity.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AsignarFuncionalidadesRequest(
        @NotEmpty(message = "Debe proporcionar al menos una funcionalidad")
        Set<Long> idsFuncionalidades
) {
}
