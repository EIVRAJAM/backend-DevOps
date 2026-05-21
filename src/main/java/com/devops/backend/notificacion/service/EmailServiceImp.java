package com.devops.backend.notificacion.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImp  implements EmailService{


    private final JavaMailSender mailSender;


    @Value("${app.mail.from:noreply@eventosapp.com}")
    private String fromAddress;


    /**
     * Envía un correo HTML con un adjunto binario opcional.
     *
     * @param to           email del destinatario
     * @param subject      asunto del correo
     * @param htmlContent  HTML ya renderizado que produce Thymeleaf
     * @param adjuntoBytes bytes del archivo adjunto
     * @param adjuntoNombre nombre del archivo adjunto (ej: "ticket-qr.png")
     */
    @Override
    public void enviarConAdjunto(String to,
                                 String subject,
                                 String htmlContent,
                                 byte[] adjuntoBytes,
                                 String adjuntoNombre) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = es HTML

            // Adjuntar QR solo si viene
            if (adjuntoBytes != null && adjuntoNombre != null) {
                helper.addAttachment(
                        adjuntoNombre,
                        new ByteArrayResource(adjuntoBytes), // envuelve byte[] en un Resource de Spring
                        "image/png"
                );
            }

            mailSender.send(mimeMessage);
            log.info("[EMAIL] Enviado correctamente → {} | Asunto: {}", to, subject);

        } catch (MessagingException e) {
            log.error("[EMAIL] Falló el envío → {} | Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error enviando correo a: " + to, e);
        }
    }
}
