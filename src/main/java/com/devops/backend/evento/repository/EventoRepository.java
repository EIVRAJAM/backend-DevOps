package com.devops.backend.evento.repository;

import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    /**
     * Busca eventos por estados del evento y general
     */
    List<Evento> findByEstadoEventoAndEstado(EstadoEvento estadoEvento, Estado estado);

    /**
     * Busca eventos publicados activos, ordenados por fecha
     */
    List<Evento> findByEstadoEventoAndEstadoOrderByFechaEventoAsc(EstadoEvento estadoEvento, Estado estado);

    /**
     * Busca un evento por ID y estado activo
     */
    Optional<Evento> findByIdEventoAndEstado(Long idEvento, Estado estado);

    /**
     * Busca eventos por usuario creador
     */
    List<Evento> findByUsuarioCreador_IdUsuario(Long idUsuario);

    /**
     * Busca eventos por usuario creador y estado
     */
    List<Evento> findByUsuarioCreador_IdUsuarioAndEstado(Long idUsuario, Estado estado);

    /**
     * Busca eventos por usuario creador y estado (paginado)
     */
    Page<Evento> findByUsuarioCreador_IdUsuarioAndEstado(Long idUsuario, Estado estado, Pageable pageable);

    /**
     * Busca eventos por rango de fechas
     */
    List<Evento> findByFechaEventoBetweenOrderByFechaEventoAsc(LocalDate inicio, LocalDate fin);

    /**
     * Busca eventos activos por rango de fechas
     */
    List<Evento> findByFechaEventoBetweenAndEstadoOrderByFechaEventoAsc(LocalDate inicio, LocalDate fin, Estado estado);

    /**
     * Busca eventos por nombre (búsqueda parcial)
     */
    List<Evento> findByNombreEventoContainsIgnoreCase(String nombre);

    /**
     * Busca eventos activos por nombre
     */
    List<Evento> findByNombreEventoContainsIgnoreCaseAndEstado(String nombre, Estado estado);

    /**
     * Busca eventos activos por lugar
     */
    List<Evento> findByLugarEventoContainsIgnoreCaseAndEstado(String lugar, Estado estado);

    /**
     * Busca eventos activos y publicados por lugar
     */
    List<Evento> findByLugarEventoContainsIgnoreCaseAndEstadoAndEstadoEvento(
            String lugar, Estado estado, EstadoEvento estadoEvento);

    /**
     * Verifica si existe un evento con un ID específico y estado activo
     */
    boolean existsByIdEventoAndEstado(Long idEvento, Estado estado);

    /**
     * Busca eventos por estado del evento
     */
    List<Evento> findByEstadoEvento(EstadoEvento estadoEvento);

    /**
     * Cuenta eventos por estado
     */
    long countByEstadoEvento(EstadoEvento estadoEvento);

    /**
     * Cuenta eventos activos por usuario creador
     */
    long countByUsuarioCreador_IdUsuarioAndEstado(Long idUsuario, Estado estado);

    /**
     * Busca eventos publicados y activos ordenados por fecha
     */
    @Query("SELECT e FROM Evento e WHERE e.estadoEvento = com.devops.backend.evento.enums.EstadoEvento.PUBLICADO " +
            "AND e.estado = com.devops.backend.evento.enums.Estado.ACTIVO ORDER BY e.fechaEvento ASC")
    List<Evento> findAllPublishedAndActive();
}
