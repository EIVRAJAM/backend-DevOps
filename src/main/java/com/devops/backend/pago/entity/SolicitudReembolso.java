package com.devops.backend.pago.entity;

import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoSolicitudReembolso;
import com.devops.backend.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "solicitudes_reembolso")
public class SolicitudReembolso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud_reembolso")
    private Long idSolicitudReembolso;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_solicitante", nullable = false)
    private Usuario usuarioSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_responsable")
    private Usuario usuarioResponsable;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false, length = 50)
    private EstadoSolicitudReembolso estadoSolicitud;

    @Column(name = "motivo_solicitud", nullable = false, columnDefinition = "TEXT")
    private String motivoSolicitud;

    @Column(name = "respuesta_organizador", columnDefinition = "TEXT")
    private String respuestaOrganizador;

    @Column(name = "monto_solicitado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoSolicitado;

    @Column(name = "monto_aprobado", precision = 10, scale = 2)
    private BigDecimal montoAprobado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @OneToOne(mappedBy = "solicitudReembolso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DatosReembolsoSolicitud datosReembolso;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        creadoEn = now;
        actualizadoEn = now;
        if (fechaSolicitud == null) {
            fechaSolicitud = now;
        }
        if (estadoSolicitud == null) {
            estadoSolicitud = EstadoSolicitudReembolso.SOLICITADA;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
