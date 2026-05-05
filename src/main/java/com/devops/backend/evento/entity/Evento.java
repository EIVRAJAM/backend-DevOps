package com.devops.backend.evento.entity;

import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import com.devops.backend.evento.enums.Moneda;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_creador", nullable = false)
    private Usuario usuarioCreador;

    @Column(name = "nombre_evento", nullable = false, length = 150)
    private String nombreEvento;

    @Column(name = "descripcion_evento", columnDefinition = "TEXT")
    private String descripcionEvento;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    @Column(name = "hora_evento", nullable = false)
    private LocalTime horaEvento;

    @Column(name = "lugar_evento", nullable = false, length = 200)
    private String lugarEvento;

    @Column(name = "referencia_ubicacion", length = 255)
    private String referenciaUbicacion;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_evento", nullable = false)
    private EstadoEvento estadoEvento;

    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;

    @Column(name = "tiene_parqueadero", nullable = false)
    private Boolean tieneParqueadero;

    @Column(name = "cupos_parqueadero")
    private Integer cuposParqueadero;

    @Column(name = "es_de_pago", nullable = false)
    private Boolean esDePago;

    @Column(name = "precio", precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", nullable = false, length = 3)
    private Moneda moneda;

    @Column(name = "capacidad_disponible")
    private Integer capacidadDisponible;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
        if (estadoEvento == null) {
            estadoEvento = EstadoEvento.BORRADOR;
        }
        if (estado == null) {
            estado = Estado.ACTIVO;
        }
        if (tieneParqueadero == null) {
            tieneParqueadero = false;
        }
        if (tieneParqueadero && cuposParqueadero == null) {
            cuposParqueadero = 0;
        }
        if (esDePago == null) {
            esDePago = false;
        }
        if (moneda == null) {
            moneda = Moneda.USD;
        }
        if (capacidadMaxima != null) {
            capacidadDisponible = capacidadMaxima;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
