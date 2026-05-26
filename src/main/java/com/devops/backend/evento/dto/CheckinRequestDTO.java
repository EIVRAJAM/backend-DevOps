package com.devops.backend.evento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Petición para realizar check-in escaneando código QR")
public record CheckinRequestDTO(
        @Schema(description = "Código QR del ticket a escanear", required = true)
        @NotBlank(message = "El código QR es obligatorio")
        String codigoQr
) {}
