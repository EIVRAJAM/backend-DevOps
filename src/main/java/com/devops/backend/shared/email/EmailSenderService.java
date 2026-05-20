package com.devops.backend.shared.email;

import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo enviar el correo HTML", ex);
        }
    }

    public void sendHtmlEmailWithAttachments(String to, String subject, String htmlContent,
            MultipartFile[] attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (attachments != null) {
                for (MultipartFile file : attachments) {
                    if (file != null && !file.isEmpty()) {
                        String filename = sanitizeFilename(file.getOriginalFilename());
                        DataSource source = new ByteArrayDataSource(file.getBytes(), file.getContentType());
                        helper.addAttachment(filename, source);
                    }
                }
            }

            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo enviar el correo HTML con adjuntos", ex);
        }
    }

    private String sanitizeFilename(String original) {
        if (original == null) return "archivo";
        return original.replaceAll("[^a-zA-Z0-9.\\-_() ]", "_").replaceAll("\\s+", "_");
    }
}
