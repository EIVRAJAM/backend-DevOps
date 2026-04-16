package com.devops.backend.auth.service;

import com.devops.backend.auth.dto.ForgotPasswordRequest;
import com.devops.backend.auth.dto.ResetPasswordRequest;
import com.devops.backend.auth.entity.PasswordResetCode;

public interface PasswordResetService {

    /**
     * Solicita un código de recuperación de contraseña para un usuario
     *
     * @param request Solicitud con el correo electrónico
     */
    void requestPasswordReset(ForgotPasswordRequest request);

    /**
     * Resetea la contraseña de un usuario usando el código y la nueva contraseña
     *
     * @param request Solicitud con email, código y nueva contraseña
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Genera un código de 6 dígitos aleatorio
     */
    String generateResetCode();

    /**
     * Valida un código de reseteo
     */
    PasswordResetCode validateResetCode(String email, String codigo);
}
