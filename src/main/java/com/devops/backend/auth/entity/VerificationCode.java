package com.devops.backend.auth.entity;

import com.devops.backend.usuario.entity.Usuario;
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
@Table(name = "verification_codes")
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_codigo")
    private Long idCodigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;

    @Column(name = "tipo_codigo")
    private String tipoCodigo;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private Boolean usado = false;

    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        if (usado == null) {
            usado = false;
        }
        if (intentos == null) {
            intentos = 0;
        }
    }

    /**
     * Verifica si el código ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * Verifica si el código puede ser usado (no expirado, no usado, intentos
     * válidos)
     */
    public boolean isValid() {
        return !isExpired() && !usado && intentos < 5;
    }

    /**
     * Incrementa los intentos fallidos
     */
    public void incrementAttempts() {
        this.intentos++;
    }

    /**
     * Marca el código como usado
     */
    public void markAsUsed() {
        this.usado = true;
    }
}
