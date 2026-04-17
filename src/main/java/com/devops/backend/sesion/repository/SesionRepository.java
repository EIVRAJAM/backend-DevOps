package com.devops.backend.sesion.repository;

import com.devops.backend.sesion.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SesionRepository extends JpaRepository<Sesion, Long>, JpaSpecificationExecutor<Sesion> {
    Optional<Sesion> findByTokenJti(String tokenJti);

    Optional<Sesion> findTopByUsuario_IdUsuarioOrderByFechaInicioDesc(Long idUsuario);

    @Modifying
    @Query("""
                UPDATE Sesion s
                SET s.activa = false,
                    s.fechaFin = :ahora
                WHERE s.activa = true
                  AND s.fechaInicio <= :limite
                  AND s.fechaFin IS NULL
            """)
    int cerrarSesionesExpiradas(
            @Param("limite") LocalDateTime limite,
            @Param("ahora") LocalDateTime ahora);
}
