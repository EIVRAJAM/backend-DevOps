package com.devops.backend.pago.entity;

import com.devops.backend.pago.enums.MedioReembolso;
import com.devops.backend.pago.enums.TipoCuentaReembolso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "datos_reembolso_solicitud")
public class DatosReembolsoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_datos_reembolso")
    private Long idDatosReembolso;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud_reembolso", nullable = false, unique = true)
    private SolicitudReembolso solicitudReembolso;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_reembolso", nullable = false, length = 30)
    private MedioReembolso medioReembolso;

    @Column(name = "titular_cuenta", nullable = false, length = 150)
    private String titularCuenta;

    @Column(name = "documento_titular", nullable = false, length = 20)
    private String documentoTitular;

    @Column(name = "entidad_financiera", length = 100)
    private String entidadFinanciera;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", length = 20)
    private TipoCuentaReembolso tipoCuenta;

    @Column(name = "numero_cuenta_enmascarado", length = 30)
    private String numeroCuentaEnmascarado;

    @Column(name = "correo_contacto", nullable = false, length = 150)
    private String correoContacto;

    @Column(name = "telefono_contacto", nullable = false, length = 30)
    private String telefonoContacto;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "certificado_enviado", nullable = false)
    private Boolean certificadoEnviado = false;

    @Column(name = "documento_adicional_enviado", nullable = false)
    private Boolean documentoAdicionalEnviado = false;

    @Column(name = "fecha_envio_datos", nullable = false)
    private LocalDateTime fechaEnvioDatos;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        creadoEn = now;
        actualizadoEn = now;
        if (fechaEnvioDatos == null) {
            fechaEnvioDatos = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
