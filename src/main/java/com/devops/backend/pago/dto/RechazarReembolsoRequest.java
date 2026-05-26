package com.devops.backend.pago.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RechazarReembolsoRequest {
    @NotBlank(message = "El comentario es obligatorio para rechazar")
    private String comentario;
}
