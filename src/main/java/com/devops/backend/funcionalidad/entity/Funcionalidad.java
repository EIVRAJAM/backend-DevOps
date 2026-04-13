package com.devops.backend.funcionalidad.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "funcionalidades")
public class Funcionalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionalidad")
    private Long idFuncionalidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_padre_funcionalidad")
    private Funcionalidad padre;

    @OneToMany(mappedBy = "padre")
    private Set<Funcionalidad> hijos;

    @Column(name = "nombre_funcionalidad", nullable = false, length = 150)
    private String nombreFuncionalidad;

    @Column(name = "url_funcionalidad", length = 250)
    private String urlFuncionalidad;

    private String estado;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}