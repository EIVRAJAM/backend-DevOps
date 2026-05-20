package com.devops.backend.evento.enums;

/**
 * Estado resumido para que el front pinte la relacion del usuario con un evento.
 */
public enum EstadoInscripcionUsuario {
    NO_INSCRITO,
    INSCRITO,
    CHECKOUT_PENDIENTE,
    PAGO_EN_PROCESO,
    REINTENTO_DISPONIBLE
}
