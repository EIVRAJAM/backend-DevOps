package com.devops.backend.notificacion.service;

public interface EmailService {

    void enviarConAdjunto(String to, String subject, String htmlContent, byte[] adjuntoBytes, String adjuntoNombre);

}
