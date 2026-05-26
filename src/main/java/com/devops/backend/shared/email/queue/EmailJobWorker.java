package com.devops.backend.shared.email.queue;

import com.devops.backend.shared.email.entity.EmailJob;
import com.devops.backend.shared.email.entity.EmailJobAttachment;
import com.devops.backend.shared.email.enums.EmailJobStatus;
import com.devops.backend.shared.email.repository.EmailJobRepository;
import com.devops.backend.shared.email.storage.EmailAttachmentStorageService;
import com.devops.backend.shared.email.template.EmailTemplateRenderer;

import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailJobWorker {

    private final EmailJobRepository emailJobRepository;
    private final EmailAttachmentStorageService storageService;
    private final EmailTemplateRenderer templateRenderer;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.email.queue.batch-size:10}")
    private int batchSize;

    @Value("${app.email.queue.enabled:true}")
    private boolean enabled;

    private static final String SENSITIVE_KEY = "numeroCuentaCompleto";

    @Scheduled(fixedDelayString = "${app.email.queue.fixed-delay-ms:10000}")
    @Transactional
    public void processEmailQueue() {
        if (!enabled) {
            return;
        }

        List<EmailJob> jobs = emailJobRepository.findPendingJobsForProcessing(
                LocalDateTime.now(),
                PageRequest.of(0, batchSize));

        if (jobs.isEmpty()) return;

        log.info("Procesando {} jobs de correo pendientes", jobs.size());

        for (EmailJob job : jobs) {
            processJob(job);
        }
    }

    private void processJob(EmailJob job) {
        job.setEstado(EmailJobStatus.PROCESANDO);
        job = emailJobRepository.save(job);

        try {
            String html = templateRenderer.render(job.getTemplate(), job.getPayload());

            sendEmail(job, html);

            clearSensitivePayload(job);

            job.setEstado(EmailJobStatus.ENVIADO);
            job.setEnviadoEn(LocalDateTime.now());
            job.setUltimoError(null);
            emailJobRepository.save(job);

            for (EmailJobAttachment attachment : job.getAttachments()) {
                storageService.delete(attachment.getStoragePath());
            }

            log.info("Job de correo enviado. id={}, tipo={}, destinatario={}",
                    job.getIdEmailJob(), job.getTipo(), job.getDestinatario());

        } catch (Exception e) {
            log.error("Error al procesar job de correo. id={}, tipo={}, intento={}/{}: {}",
                    job.getIdEmailJob(), job.getTipo(),
                    job.getIntentos() + 1, job.getMaxIntentos(),
                    e.getMessage());

            job.setIntentos(job.getIntentos() + 1);
            job.setUltimoError(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());

            if (job.getIntentos() >= job.getMaxIntentos()) {
                job.setEstado(EmailJobStatus.FALLIDO);
                log.warn("Job de correo FALLIDO definitivamente. id={}, tipo={}", job.getIdEmailJob(), job.getTipo());
            } else {
                job.setEstado(EmailJobStatus.PENDIENTE);
                long backoffMinutes = (long) Math.pow(2, job.getIntentos());
                job.setProximoIntentoEn(LocalDateTime.now().plusMinutes(backoffMinutes));
            }

            emailJobRepository.save(job);
        }
    }

    private void clearSensitivePayload(EmailJob job) {
        Map<String, Object> payload = job.getPayload();
        if (payload != null && payload.containsKey(SENSITIVE_KEY)) {
            payload.put(SENSITIVE_KEY, "***ELIMINADO***");
            job.setPayload(payload);
        }
    }

    private void sendEmail(EmailJob job, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(job.getDestinatario());
        if (job.getCopia() != null && !job.getCopia().isBlank()) {
            helper.setCc(job.getCopia().split(","));
        }
        helper.setSubject(job.getAsunto());
        helper.setText(html, true);

        if (job.getAttachments() != null && !job.getAttachments().isEmpty()) {
            for (EmailJobAttachment attachment : job.getAttachments()) {
                File file = new File(attachment.getStoragePath());
                if (file.exists()) {
                    DataSource source = new ByteArrayDataSource(
                            new FileInputStream(file), attachment.getContentType());
                    helper.addAttachment(attachment.getNombreOriginal(), source);
                }
            }
        }

        mailSender.send(message);
    }
}
