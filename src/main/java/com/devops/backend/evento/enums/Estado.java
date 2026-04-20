package com.devops.backend.evento.enums;

/**
 * Enum que representa el estado general de entidades en el sistema.
 * Sigue el patrón Type-Safe Enum.
 *
 * Estados permitidos:
 * - ACTIVO: Recurso activo y disponible
 * - INACTIVO: Recurso inactivo o deshabilitado
 */
public enum Estado {
    ACTIVO,
    INACTIVO
}
