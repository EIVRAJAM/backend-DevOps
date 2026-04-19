package com.devops.backend.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@devops.com}")
    private String fromEmail;

    @Value("${app.name:DevOps Backend}")
    private String appName;

    @Override
    public void sendPasswordResetEmail(String email, String codigo, int minutos) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Recuperación de Contraseña - Código de Verificación");

            String cuerpo = construirCuerpoPasswordReset(codigo, minutos);
            helper.setText(cuerpo, true);

            mailSender.send(message);
            log.info("Correo de recuperación de contraseña enviado a: {}", email);

        } catch (MessagingException e) {
            log.error("Error al enviar correo de recuperación de contraseña a: {}", email, e);
            throw new RuntimeException("No se pudo enviar el correo de recuperación de contraseña", e);
        }
    }

    @Override
    public void sendAccountUnlockEmail(String email, String codigo, int minutos) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Desbloqueo de Cuenta - Código de Verificación");

            String cuerpo = construirCuerpoAccountUnlock(codigo, minutos);
            helper.setText(cuerpo, true);

            mailSender.send(message);
            log.info("Correo de desbloqueo de cuenta enviado a: {}", email);

        } catch (MessagingException e) {
            log.error("Error al enviar correo de desbloqueo de cuenta a: {}", email, e);
            throw new RuntimeException("No se pudo enviar el correo de desbloqueo de cuenta", e);
        }
    }

    @Override
    public void sendEmail(String email, String asunto, String cuerpo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(asunto);
            helper.setText(cuerpo, true);

            mailSender.send(message);
            log.info("Correo enviado a: {}", email);

        } catch (MessagingException e) {
            log.error("Error al enviar correo a: {}", email, e);
            throw new RuntimeException("No se pudo enviar el correo", e);
        }
    }

    /**
     * Construye el cuerpo del correo para recuperación de contraseña
     */
    private String construirCuerpoPasswordReset(String codigo, int minutos) {
        return String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <style>\n" +
                        "        body { font-family: Arial, sans-serif; background-color: #f5f5f5; }\n" +
                        "        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n"
                        +
                        "        .header { text-align: center; border-bottom: 2px solid #007bff; padding-bottom: 20px; margin-bottom: 20px; }\n"
                        +
                        "        .header h1 { color: #333; margin: 0; }\n" +
                        "        .content { color: #555; line-height: 1.6; }\n" +
                        "        .code-box { background-color: #f0f0f0; border: 2px solid #007bff; border-radius: 4px; padding: 15px; text-align: center; margin: 20px 0; }\n"
                        +
                        "        .code { font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px; font-family: 'Courier New', monospace; }\n"
                        +
                        "        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 10px 15px; margin: 15px 0; border-radius: 4px; color: #856404; }\n"
                        +
                        "        .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #999; }\n"
                        +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"container\">\n" +
                        "        <div class=\"header\">\n" +
                        "            <h1>%s</h1>\n" +
                        "        </div>\n" +
                        "        <div class=\"content\">\n" +
                        "            <p>Hola,</p>\n" +
                        "            <p>Hemos recibido una solicitud para recuperar tu contraseña. Usa el siguiente código para continuar:</p>\n"
                        +
                        "            <div class=\"code-box\">\n" +
                        "                <div class=\"code\">%s</div>\n" +
                        "            </div>\n" +
                        "            <p><strong>Este código expirará en %d minutos.</strong></p>\n" +
                        "            <div class=\"warning\">\n" +
                        "                <strong>⚠️ Seguridad:</strong> Nunca compartas este código con nadie. Nuestro equipo nunca te pedirá este código por correo o teléfono.\n"
                        +
                        "            </div>\n" +
                        "            <p>Si no solicitaste recuperar tu contraseña, puedes ignorar este correo. Tu cuenta permanecerá segura.</p>\n"
                        +
                        "        </div>\n" +
                        "        <div class=\"footer\">\n" +
                        "            <p>Este es un correo automatizado. Por favor, no respondas a este mensaje.</p>\n" +
                        "            <p>&copy; 2026 %s. Todos los derechos reservados.</p>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>",
                appName,
                codigo,
                minutos,
                appName);
    }

    /**
     * Construye el cuerpo del correo para desbloqueo de cuenta
     */
    private String construirCuerpoAccountUnlock(String codigo, int minutos) {
        return String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <style>\n" +
                        "        body { font-family: Arial, sans-serif; background-color: #f5f5f5; }\n" +
                        "        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n"
                        +
                        "        .header { text-align: center; border-bottom: 2px solid #28a745; padding-bottom: 20px; margin-bottom: 20px; }\n"
                        +
                        "        .header h1 { color: #333; margin: 0; }\n" +
                        "        .content { color: #555; line-height: 1.6; }\n" +
                        "        .code-box { background-color: #f0f0f0; border: 2px solid #28a745; border-radius: 4px; padding: 15px; text-align: center; margin: 20px 0; }\n"
                        +
                        "        .code { font-size: 32px; font-weight: bold; color: #28a745; letter-spacing: 5px; font-family: 'Courier New', monospace; }\n"
                        +
                        "        .warning { background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 10px 15px; margin: 15px 0; border-radius: 4px; color: #721c24; }\n"
                        +
                        "        .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #999; }\n"
                        +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"container\">\n" +
                        "        <div class=\"header\">\n" +
                        "            <h1>%s</h1>\n" +
                        "        </div>\n" +
                        "        <div class=\"content\">\n" +
                        "            <p>Hola,</p>\n" +
                        "            <p>Tu cuenta ha sido bloqueada por medidas de seguridad. Para desbloquearla, usa el siguiente código de verificación:</p>\n"
                        +
                        "            <div class=\"code-box\">\n" +
                        "                <div class=\"code\">%s</div>\n" +
                        "            </div>\n" +
                        "            <p><strong>Este código expirará en %d minutos.</strong></p>\n" +
                        "            <div class=\"warning\">\n" +
                        "                <strong>⚠️ Importante:</strong> Si no solicitaste desbloquear tu cuenta, por favor contacta con nuestro equipo de soporte inmediatamente.\n"
                        +
                        "            </div>\n" +
                        "            <p>Nunca compartamos este código con nadie. Nuestro equipo nunca te pedirá este código por correo o teléfono.</p>\n"
                        +
                        "        </div>\n" +
                        "        <div class=\"footer\">\n" +
                        "            <p>Este es un correo automatizado. Por favor, no respondas a este mensaje.</p>\n" +
                        "            <p>&copy; 2026 %s. Todos los derechos reservados.</p>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>",
                appName,
                codigo,
                minutos,
                appName);
    }
}
