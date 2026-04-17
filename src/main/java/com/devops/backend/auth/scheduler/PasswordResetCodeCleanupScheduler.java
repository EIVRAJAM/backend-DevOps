package com.devops.backend.auth.scheduler;

import com.devops.backend.auth.repository.VerificationCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class PasswordResetCodeCleanupScheduler {

    @Autowired
    private VerificationCodeRepository passwordResetCodeRepository;

    /**
     * Limpia los códigos de reseteo expirados cada 30 minutos
     * Se ejecuta cada 30 minutos (1800000 ms)
     */
    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredCodes() {
        try {
            log.info("Iniciando limpieza de códigos de reseteo expirados...");
            passwordResetCodeRepository.deleteExpiredCodes(LocalDateTime.now());
            log.info("Limpieza de códigos expirados completada exitosamente");
        } catch (Exception e) {
            log.error("Error al limpiar códigos de reseteo expirados", e);
        }
    }

    /**
     * Alternativa: Limpieza configurada por cron
     * Se ejecuta cada hora a la media hora (30 minutos después de la hora)
     * Formato: segundo, minuto, hora, día_del_mes, mes, día_de_la_semana
     */
    // @Scheduled(cron = "0 30 * * * *")
    // public void cleanupExpiredCodesCron() {
    // try {
    // log.info("Iniciando limpieza de códigos de reseteo expirados (cron)...");
    // passwordResetCodeRepository.deleteExpiredCodes(LocalDateTime.now());
    // log.info("Limpieza de códigos expirados completada exitosamente");
    // } catch (Exception e) {
    // log.error("Error al limpiar códigos de reseteo expirados", e);
    // }
    // }
}
