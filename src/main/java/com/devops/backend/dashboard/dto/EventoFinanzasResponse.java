package com.devops.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos financieros agregados de un evento especifico")
public record EventoFinanzasResponse(
        @Schema(description = "ID del evento", example = "5") Long eventoId,
        @Schema(description = "Nombre del evento", example = "Conferencia DevOps") String nombreEvento,
        @Schema(description = "Tickets vendidos (pagados + gratis)", example = "30") long ticketsVendidos,
        @Schema(description = "Tickets gratuitos", example = "10") long ticketsGratis,
        @Schema(description = "Tickets pagados", example = "20") long ticketsPagados,
        @Schema(description = "Ingresos totales del evento", example = "1000000") BigDecimal ingresosTotales,
        @Schema(description = "Solicitudes de reembolso pendientes", example = "2") long reembolsosSolicitados,
        @Schema(description = "Monto total de reembolsos pendientes", example = "100000") BigDecimal montoReembolsosPendientes,
        @Schema(description = "Porcentaje de ocupacion (tickets vendidos / capacidad)", example = "75") double tasaOcupacion
) {
}
