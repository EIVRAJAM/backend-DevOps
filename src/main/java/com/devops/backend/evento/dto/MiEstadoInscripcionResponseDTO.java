package com.devops.backend.evento.dto;

import com.devops.backend.evento.enums.EstadoInscripcionUsuario;
import com.devops.backend.evento.enums.EstadoTicket;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estado de inscripcion del usuario autenticado para un evento")
public record MiEstadoInscripcionResponseDTO(

        @Schema(description = "ID del evento consultado", example = "3")
        Long eventoId,

        @Schema(description = "Indica si el usuario ya tiene una inscripcion confirmada", example = "true")
        Boolean inscrito,

        @Schema(description = "Indica si el usuario puede iniciar una nueva inscripcion", example = "false")
        Boolean puedeInscribirse,

        @Schema(description = "ID del ticket relacionado, si existe", example = "15")
        Long ticketId,

        @Schema(description = "Estado real del ticket, si existe",
                allowableValues = {"PENDIENTE", "PAGADO", "CANCELADO", "REEMBOLSADO", "GRATIS", "EXPIRADO"})
        EstadoTicket estadoTicket,

        @Schema(description = "Estado resumido para decisiones de interfaz",
                allowableValues = {"NO_INSCRITO", "INSCRITO", "CHECKOUT_PENDIENTE", "PAGO_EN_PROCESO", "REINTENTO_DISPONIBLE"})
        EstadoInscripcionUsuario estadoInscripcion,

        @Schema(description = "Fecha de expiracion si hay checkout pendiente")
        LocalDateTime expiraEn,

        @Schema(description = "Indica si el ticket ya realizo check-in")
        Boolean checkinRealizado
) {}
