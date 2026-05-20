package com.devops.backend.shared.email;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueService {

    private final EmailJobRepository emailJobRepository;
    private final EmailAttachmentStorageService storageService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueueHtmlEmail(
            EmailJobType tipo,
            String destinatario,
            String asunto,
            String template,
            Object payload) {
        EmailJob job = new EmailJob();
        job.setTipo(tipo);
        job.setDestinatario(destinatario);
        job.setAsunto(asunto);
        job.setTemplate(template);
        job.setPayload(toPayloadMap(payload));
        job.setEstado(EmailJobStatus.PENDIENTE);
        job.setIntentos(0);
        job.setMaxIntentos(3);
        job.setProximoIntentoEn(LocalDateTime.now());

        emailJobRepository.save(job);
        log.info("Job de correo encolado. tipo={}, destinatario={}", tipo, destinatario);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueueHtmlEmailWithAttachments(
            EmailJobType tipo,
            String destinatario,
            String asunto,
            String template,
            Object payload,
            MultipartFile... files) {
        EmailJob job = new EmailJob();
        job.setTipo(tipo);
        job.setDestinatario(destinatario);
        job.setAsunto(asunto);
        job.setTemplate(template);
        job.setPayload(toPayloadMap(payload));
        job.setEstado(EmailJobStatus.PENDIENTE);
        job.setIntentos(0);
        job.setMaxIntentos(3);
        job.setProximoIntentoEn(LocalDateTime.now());

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    EmailAttachmentStorageService.StoredAttachment stored = storageService.store(file);
                    if (stored != null) {
                        EmailJobAttachment attachment = new EmailJobAttachment();
                        attachment.setEmailJob(job);
                        attachment.setNombreOriginal(stored.nombreOriginal());
                        attachment.setNombreAlmacenado(stored.nombreAlmacenado());
                        attachment.setContentType(stored.contentType());
                        attachment.setSizeBytes(stored.sizeBytes());
                        attachment.setStoragePath(stored.storagePath());
                        job.getAttachments().add(attachment);
                    }
                }
            }
        }

        emailJobRepository.save(job);
        log.info("Job de correo con adjuntos encolado. tipo={}, destinatario={}, adjuntos={}",
                tipo, destinatario, job.getAttachments().size());
    }

    private Map<String, Object> toPayloadMap(Object payload) {
        return objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});
    }
}
