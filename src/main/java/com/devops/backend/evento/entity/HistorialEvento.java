package com.devops.backend.evento.entity;

import com.devops.backend.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad JPA para auditoría de cambios de estado en eventos
 * Tabla: historial_eventos
 * Inmutable por diseño: solo se inserta, nunca se actualiza
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "historial_eventos")
public class HistorialEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_evento")
    private Long idHistorialEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    @Column(name = "estado_anterior", length = 10)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 10)
    private String estadoNuevo;

    @Column(name = "comentario", length = 500)
    private String comentario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_responsable")
    private Usuario usuarioResponsable;

    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;

    @PrePersist
    protected void onCreate() {
        if (fechaCambio == null) {
            fechaCambio = LocalDateTime.now();
        }
    }
}
