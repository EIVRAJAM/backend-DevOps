package com.devops.backend.auth.service;

import com.devops.backend.auth.dto.UnlockAccountRequest;

public interface AccountUnlockService {
    /**
     * Desbloquea una cuenta validando el código de verificación
     *
     * @param request Solicitud con email y código de verificación
     * @throws com.devops.backend.exception.BadRequestException si la validación
     *                                                          falla
     */
    void unlockAccount(UnlockAccountRequest request);

    /**
     * Solicita el desbloqueo de una cuenta enviando un código por email
     *
     * @param email Email del usuario para desbloquear
     * @throws com.devops.backend.exception.BadRequestException si la cuenta no
     *                                                          existe o no está
     *                                                          bloqueada
     */
    void requestAccountUnlock(String email);
}
