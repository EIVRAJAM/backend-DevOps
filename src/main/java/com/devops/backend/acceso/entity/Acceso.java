package com.devops.backend.acceso.entity;

import com.devops.backend.usuario.entity.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "accesos")
public class Acceso {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @MapsId
    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "correo_acceso", nullable = false, unique = true, length = 200)
    private String correoAcceso;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false)
    private String claveAcceso;

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos = 0;

    @Column(name = "estado_cuenta", length = 20, nullable = false)
    private String estadoCuenta = "ACTIVO";

    @Column(name = "uuid_acceso", nullable = false, updatable = false)
    private UUID uuidAcceso;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        // actualizadoEn = LocalDateTime.now();

        if (intentosFallidos == null)
            intentosFallidos = 0;

        if (estadoCuenta == null)
            estadoCuenta = "ACTIVO";

        if (uuidAcceso == null)
            uuidAcceso = UUID.randomUUID();
    }
}
