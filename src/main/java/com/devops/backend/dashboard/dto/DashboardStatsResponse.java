package com.devops.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Datos agregados para el dashboard administrativo")
public record DashboardStatsResponse(
        @Schema(description = "Total de usuarios registrados", example = "15") long totalUsuarios,
        @Schema(description = "Usuarios con estado ACTIVO", example = "12") long usuariosActivos,
        @Schema(description = "Total de eventos creados", example = "8") long totalEventos,
        @Schema(description = "Eventos con estado PUBLICADO", example = "5") long eventosPublicados,
        @Schema(description = "Total de tickets emitidos", example = "45") long totalTickets,
        @Schema(description = "Tickets vendidos en el dia de hoy", example = "3") long ticketsVendidosHoy,
        @Schema(description = "Total de pagos registrados", example = "20") long totalPagos,
        @Schema(description = "Monto total acumulado de pagos", example = "1250000") BigDecimal montoTotalPagos,
        @Schema(description = "Solicitudes de reembolso pendientes", example = "2") long reembolsosPendientes,
        @Schema(description = "Sesiones activas en el sistema", example = "4") long sesionesActivas,
        @Schema(description = "Ultimos 5 usuarios registrados") List<UltimoUsuarioItem> ultimosUsuarios,
        @Schema(description = "Proximos 5 eventos publicados") List<ProximoEventoItem> proximosEventos
) {
}
