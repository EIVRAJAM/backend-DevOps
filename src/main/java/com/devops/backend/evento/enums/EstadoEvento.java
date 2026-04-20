package com.devops.backend.evento.enums;

/**
 * Enum que representa los estados del ciclo de vida de un evento.
 * Sigue el patrón Type-Safe Enum.
 *
 * Estados permitidos:
 * - BORRADOR: Evento en creación, no visible públicamente
 * - PUBLICADO: Evento visible y abierto para inscripciones
 * - CERRADO: Evento finalizado, no acepta más inscripciones
 * - CANCELADO: Evento cancelado
 */
public enum EstadoEvento {
    BORRADOR,
    PUBLICADO,
    CERRADO,
    CANCELADO
}

