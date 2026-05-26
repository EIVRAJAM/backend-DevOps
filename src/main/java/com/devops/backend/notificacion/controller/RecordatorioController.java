package com.devops.backend.notificacion.controller;

import com.devops.backend.notificacion.service.RecordatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
@Tag(name = "Notificaciones", description = "Disparo manual de recordatorios y notificaciones")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    @PostMapping("/recordatorios/ejecutar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Ejecutar recordatorios manualmente",
            description = "Procesa los recordatorios para eventos de manana. "
                    + "Equivalente a la ejecucion programada diaria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recordatorios procesados exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores")
    })
    public ResponseEntity<Map<String, Object>> ejecutarRecordatorios() {
        int procesados = recordatorioService.procesarRecordatorios();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Recordatorios procesados",
                "ticketsProcesados", procesados
        ));
    }
}
