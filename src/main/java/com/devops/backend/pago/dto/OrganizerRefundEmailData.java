package com.devops.backend.pago.dto;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record OrganizerRefundEmailData(
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
        Long idSolicitud,
        MultipartFile certificadoCuenta,
        MultipartFile documentoAdicional
) {
}
