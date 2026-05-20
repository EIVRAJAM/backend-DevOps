package com.devops.backend.pago.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolicitudReembolsoRequest {
    @NotBlank(message = "El motivo de la solicitud es obligatorio")
    private String motivoSolicitud;
}
