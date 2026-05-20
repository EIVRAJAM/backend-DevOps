package com.devops.backend.pago.dto;

public record DatosReembolsoResponse(
        String medioReembolso,
        String titularCuenta,
        String documentoTitular,
        String entidadFinanciera,
        String tipoCuenta,
        String numeroCuentaEnmascarado,
        String correoContacto,
        String telefonoContacto,
        String observaciones
) {
}
