package com.devops.backend.usuario.dto;

import java.util.List;

public record CompleteStatusResponse(
        boolean requiresCompletion,
        List<String> missingFields
) {
}
