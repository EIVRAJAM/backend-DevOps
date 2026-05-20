package com.devops.backend.pago.dto;

import java.math.BigDecimal;

public record OrganizerRefundEmailPayload(
        String emailOrganizador,
        String eventoNombre,
        String usuario,
        String emailUsuario,
        BigDecimal monto,
        String motivo,
        String medioReembolso,
        String titularCuenta,
        String documentoTitular,
        String entidadFinanciera,
        String tipoCuenta,
        String numeroCuentaCompleto,
        String correoContacto,
        String telefonoContacto,
        String observaciones,
        Long idSolicitud
) {
    public static OrganizerRefundEmailPayload from(OrganizerRefundEmailData data) {
        return new OrganizerRefundEmailPayload(
                data.emailOrganizador(),
                data.eventoNombre(),
                data.usuario(),
                data.emailUsuario(),
                data.monto(),
                data.motivo(),
                data.medioReembolso(),
                data.titularCuenta(),
                data.documentoTitular(),
                data.entidadFinanciera(),
                data.tipoCuenta(),
                data.numeroCuentaCompleto(),
                data.correoContacto(),
                data.telefonoContacto(),
                data.observaciones(),
                data.idSolicitud()
        );
    }
}
