package com.devops.backend.pago.dto;

import java.math.BigDecimal;

public record RefundEmailData(
        String email,
        String eventoNombre,
        BigDecimal monto,
        String comentarioOrganizador,
        Long idSolicitud
) {
}
