package com.devops.backend.evento.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen de check-in del evento")
public record CheckinResumenDTO(
        @Schema(description = "Total de personas inscritas válidas (GRATIS o PAGADO)", example = "100")
        long totalInscritos,
        
        @Schema(description = "Total de personas que ya ingresaron", example = "45")
        long totalIngresados,
        
        @Schema(description = "Total de personas pendientes por ingresar", example = "55")
        long totalPendientes,
        
        @Schema(description = "Porcentaje de ingreso (0 a 100)", example = "45.0")
        double porcentajeIngreso
) {}
