package com.devops.backend.evento.enums;

/**
 * Estados posibles de un ticket.
 * - PENDIENTE: Esperando pago o procesamiento
 * - PAGADO: Transacción completada exitosamente
 * - CANCELADO: Usuario o sistema canceló la compra
 * - REEMBOLSADO: Se devolvió el dinero al usuario
 * - GRATIS: Evento sin costo (inscripción directa)
 */
public enum EstadoTicket {
    PENDIENTE,
    PAGADO,
    CANCELADO,
    REEMBOLSADO,
    GRATIS
}
