package com.devops.backend.pago.dto;

import com.devops.backend.evento.enums.EstadoSolicitudReembolso;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudReembolsoResponse {
    private Long idSolicitud;
    private Long idTicket;
    private Long idEvento;
    private Long idUsuarioSolicitante;
    private EstadoSolicitudReembolso estado;
    private String motivo;
    private String respuestaOrganizador;
    private BigDecimal montoSolicitado;
    private BigDecimal montoAprobado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRevision;
    private LocalDateTime fechaProcesamiento;
    private DatosReembolsoResponse datosReembolso;
}
