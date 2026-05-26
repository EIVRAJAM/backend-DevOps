package com.devops.backend.dashboard.controller;

import com.devops.backend.dashboard.dto.DashboardStatsResponse;
import com.devops.backend.dashboard.dto.EventoFinanzasResponse;
import com.devops.backend.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints de estadisticas y metricas agregadas para el dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    @Operation(
            summary = "Estadisticas generales - ADMIN",
            description = "Devuelve datos agregados del sistema: totales de usuarios, eventos, tickets, pagos, "
                    + "sesiones activas, reembolsos pendientes, ultimos usuarios y proximos eventos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estadisticas recuperadas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores")
    })
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @GetMapping("/eventos/{id}/finanzas")
    @Operation(
            summary = "Finanzas por evento - ORGANIZER/ADMIN",
            description = "Devuelve datos financieros agregados de un evento: tickets vendidos, gratuitos, pagados, "
                    + "ingresos totales, reembolsos pendientes y tasa de ocupacion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Finanzas recuperadas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo organizadores y administradores"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<EventoFinanzasResponse> getFinanzasByEvento(
            @PathVariable @Parameter(description = "ID del evento", required = true, example = "5") Long id) {
        return ResponseEntity.ok(dashboardService.getFinanzasByEvento(id));
    }
}
