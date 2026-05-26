package com.devops.backend.notificacion.scheduler;

import com.devops.backend.notificacion.service.RecordatorioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioEventoScheduler {

    private final RecordatorioService recordatorioService;

    @Scheduled(cron = "0 0 10 * * *")
    public void enviarRecordatorios() {
        log.info("[SCHEDULER] Iniciando envio de recordatorios de eventos...");
        int publicados = recordatorioService.procesarRecordatorios();
        log.info("[SCHEDULER] Recordatorios publicados: {}", publicados);
    }
}
