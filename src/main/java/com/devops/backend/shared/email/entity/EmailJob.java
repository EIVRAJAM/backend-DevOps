package com.devops.backend.shared.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.devops.backend.shared.email.enums.EmailJobStatus;
import com.devops.backend.shared.email.enums.EmailJobType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "email_jobs")
public class EmailJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_email_job")
    private Long idEmailJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 80)
    private EmailJobType tipo;

    @Column(name = "destinatario", nullable = false, length = 200)
    private String destinatario;

    @Column(name = "copia", length = 500)
    private String copia;

    @Column(name = "asunto", nullable = false, length = 250)
    private String asunto;

    @Column(name = "template", nullable = false, length = 150)
    private String template;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EmailJobStatus estado;

    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    @Column(name = "max_intentos", nullable = false)
    private Integer maxIntentos = 3;

    @Column(name = "proximo_intento_en", nullable = false)
    private LocalDateTime proximoIntentoEn;

    @Column(name = "enviado_en")
    private LocalDateTime enviadoEn;

    @Column(name = "ultimo_error", columnDefinition = "TEXT")
    private String ultimoError;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy = "emailJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmailJobAttachment> attachments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        creadoEn = now;
        actualizadoEn = now;
        if (estado == null) {
            estado = EmailJobStatus.PENDIENTE;
        }
        if (intentos == null) {
            intentos = 0;
        }
        if (maxIntentos == null) {
            maxIntentos = 3;
        }
        if (proximoIntentoEn == null) {
            proximoIntentoEn = now;
        }
        if (payload == null) {
            payload = new HashMap<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
