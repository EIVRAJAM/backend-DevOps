package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.enums.Moneda;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta completa de un ticket.
 * Se usa en GET mis-tickets, GET {id}, GET eventos/{id}/tickets.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informacion completa de un ticket de inscripcion")
public record TicketResponseDTO(

        @Schema(description = "ID unico del ticket", example = "1")
        Long idTicket,

        @Schema(description = "ID del evento al que pertenece el ticket", example = "5")
        Long idEvento,

        @Schema(description = "Nombre del evento", example = "Conferencia DevOps 2026")
        String nombreEvento,

        @Schema(description = "ID del usuario propietario del ticket", example = "12")
        Long idUsuario,

        @Schema(description = "Estado actual del ticket",
                example = "GRATIS",
                allowableValues = {"PENDIENTE", "PAGADO", "CANCELADO", "REEMBOLSADO", "GRATIS", "EXPIRADO"})
        EstadoTicket estadoTicket,

        @Schema(description = "Monto pagado por el ticket", example = "50000.00")
        BigDecimal montoPagado,

        @Schema(description = "Moneda del pago", example = "COP",
                allowableValues = {"USD", "COP", "EUR", "MXN"})
        Moneda moneda,

        @Schema(description = "Codigo QR unico del ticket", example = "uuid-generado")
        String codigoQr,

        @Schema(description = "Fecha de compra del ticket")
        LocalDateTime fechaCompra,

        @Schema(description = "Fecha de expiracion del checkout si el ticket esta pendiente")
        LocalDateTime expiraEn,

        @Schema(description = "Fecha de creacion del registro")
        LocalDateTime creadoEn
) {}
