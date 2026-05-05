package com.devops.backend.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;


public record CompleteStatusResponse(
        @Schema(
                description = "Indica si el usuario necesita completar su perfil",
                example = "true"
        )
        boolean requiresCompletion,
        
        @Schema(
                description = "Lista de nombres de campos que faltan completar",
                example = "[\"firstName\", \"lastName\", \"phone\"]"
        )
        List<String> missingFields
) {
}
