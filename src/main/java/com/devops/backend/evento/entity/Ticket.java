package com.devops.backend.evento.entity;

import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.enums.Moneda;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la inscripción de un usuario a un evento.
 * Aplica tanto para eventos gratuitos como de pago.
 *
 * Restricciones de integridad:
 * - Un usuario no puede tener dos tickets activos para el mismo evento
 *   (indice unico parcial en BD para PENDIENTE/PAGADO/GRATIS)
 * - Preserva historial incluso si evento o usuario se eliminan (ON DELETE
 * RESTRICT)
 * - Para eventos de pago, monto_pagado y moneda son obligatorios
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Long idTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;

    @Column(name = "stripe_client_secret", length = 255)
    private String stripeClientSecret;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ticket", nullable = false)
    private EstadoTicket estadoTicket;

    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", length = 3)
    private Moneda moneda;

    @Column(name = "codigo_qr", length = 255)
    private String codigoQr;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    @Column(name = "checkin_realizado", nullable = false)
    private Boolean checkinRealizado = false;

    @Column(name = "fecha_checkin")
    private LocalDateTime fechaCheckin;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_checkin")
    private Usuario usuarioCheckin;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
        if (fechaCompra == null) {
            fechaCompra = LocalDateTime.now();
        }
        if (estadoTicket == null) {
            estadoTicket = EstadoTicket.PENDIENTE;
        }

    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
