package com.devops.backend.notificacion.controller;

import com.devops.backend.notificacion.scheduler.RecordatorioEventoScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class RecordatorioController {

    private final RecordatorioEventoScheduler scheduler;

    @PostMapping("/recordatorios/ejecutar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> ejecutarRecordatorios() {
        int enviados = scheduler.procesarRecordatorios();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Recordatorios procesados",
                "ticketsProcesados", enviados
        ));
    }
}