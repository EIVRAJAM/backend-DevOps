package com.devops.backend.usuario.entity;

import com.devops.backend.rol.entity.*;
import com.devops.backend.acceso.entity.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @OneToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "documento_usuario", unique = true, length = 150)
    private String documento;

    @Column(name = "nombres_usuario", nullable = false, length = 150)
    private String nombres;

    @Column(name = "apellidos_usuario", nullable = false, length = 150)
    private String apellidos;

    @Column(name = "genero_usuario")
    private Short genero;

    @Column(name = "fecha_nacimiento_usuario")
    private LocalDate fechaNacimiento;

    @Column(name = "telefono_usuario", length = 50)
    private String telefono;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @OneToOne(mappedBy = "usuario")
    private Acceso acceso;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
        estado = "ACTIVO"; // Valor por defecto al crear un nuevo usuario
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
