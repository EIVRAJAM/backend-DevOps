package com.devops.backend.evento.enums;

/**
 * Estados posibles de un ticket.
 * - PENDIENTE: esperando pago o procesamiento.
 * - PAGADO: transaccion completada exitosamente.
 * - CANCELADO: usuario o sistema cancelo la compra.
 * - REEMBOLSADO: se devolvio el dinero al usuario.
 * - GRATIS: evento sin costo con inscripcion directa.
 * - EXPIRADO: intento de pago pendiente vencido sin confirmacion.
 */
public enum EstadoTicket {
    PENDIENTE,
    PAGADO,
    CANCELADO,
    REEMBOLSADO,
    GRATIS,
    EXPIRADO
}
