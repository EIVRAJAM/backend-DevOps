package com.devops.backend.auth.service;

public interface EmailService {

    /**
     * Envía un correo electrónico de recuperación de contraseña
     *
     * @param email   Correo destino
     * @param codigo  Código de 6 dígitos
     * @param minutos Minutos de expiración del código
     */
    void sendPasswordResetEmail(String email, String codigo, int minutos);

    /**
     * Envía un correo electrónico de desbloqueo de cuenta
     *
     * @param email   Correo destino
     * @param codigo  Código de 6 dígitos
     * @param minutos Minutos de expiración del código
     */
    void sendAccountUnlockEmail(String email, String codigo, int minutos);

    /**
     * Envía un correo electrónico genérico
     *
     * @param email  Correo destino
     * @param asunto Asunto del correo
     * @param cuerpo Cuerpo del correo (HTML)
     */
    void sendEmail(String email, String asunto, String cuerpo);
}
