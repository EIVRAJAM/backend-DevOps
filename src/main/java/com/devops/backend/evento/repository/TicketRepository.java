package com.devops.backend.evento.repository;

import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByUsuario_IdUsuarioAndEvento_IdEventoAndEstadoTicketIn(
            Long idUsuario,
            Long idEvento,
            List<EstadoTicket> estadosTicket);

    Optional<Ticket> findFirstByUsuario_IdUsuarioAndEvento_IdEventoAndEstadoTicketOrderByFechaCompraDesc(
            Long idUsuario,
            Long idEvento,
            EstadoTicket estadoTicket);

    Optional<Ticket> findFirstByUsuario_IdUsuarioAndEvento_IdEventoOrderByFechaCompraDesc(
            Long idUsuario,
            Long idEvento);

    /** Todos los tickets de un usuario (mis-tickets), ordenados por fecha desc */
    List<Ticket> findByUsuario_IdUsuarioOrderByFechaCompraDesc(Long idUsuario);

    /** Todos los tickets de un evento (vista organizador/admin), ordenados por fecha desc */
    List<Ticket> findByEvento_IdEventoOrderByFechaCompraDesc(Long idEvento);

    /** Todos los tickets activos (no cancelados/reembolsados) de un evento */
    List<Ticket> findByEvento_IdEventoAndEstadoTicketNotIn(Long idEvento, List<EstadoTicket> excluidos);

    /**
     * Descuenta 1 cupo disponible de forma atómica con una sola sentencia UPDATE,
     * sólo si quedan cupos (capacidadDisponible > 0).
     * Devuelve el número de filas afectadas (1 = éxito, 0 = sin cupos).
     */
    @Modifying
    @Query("UPDATE Evento e SET e.capacidadDisponible = e.capacidadDisponible - 1 " +
           "WHERE e.idEvento = :eventoId AND e.capacidadDisponible > 0")
    int decrementarCupo(@Param("eventoId") Long eventoId);

    /**
     * Incrementa 1 cupo disponible de forma atómica, usado al liberar cupo
     * tras un reembolso.
     */
    @Modifying
    @Query("UPDATE Evento e SET e.capacidadDisponible = e.capacidadDisponible + 1 " +
           "WHERE e.idEvento = :eventoId")
    int incrementarCupo(@Param("eventoId") Long eventoId);

    Optional<Ticket> findByEvento_IdEventoAndCodigoQr(Long idEvento, String codigoQr);

    long countByEvento_IdEventoAndEstadoTicketNotIn(Long idEvento, List<EstadoTicket> excluidos);

    long countByEvento_IdEventoAndCheckinRealizadoTrue(Long idEvento);

    @Modifying
    @Query("UPDATE Ticket t SET t.estadoTicket = :estadoExpirado " +
           "WHERE t.estadoTicket = :estadoPendiente " +
           "AND t.expiraEn IS NOT NULL " +
           "AND t.expiraEn <= :ahora")
    int expirarTicketsPendientesVencidos(
            @Param("ahora") java.time.LocalDateTime ahora,
            @Param("estadoPendiente") EstadoTicket estadoPendiente,
            @Param("estadoExpirado") EstadoTicket estadoExpirado);

    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.evento " +
           "WHERE t.estadoTicket = :estadoPendiente " +
           "AND t.expiraEn IS NOT NULL " +
           "AND t.expiraEn <= :ahora " +
           "ORDER BY t.expiraEn ASC")
    List<Ticket> findTicketsPendientesVencidos(
            @Param("ahora") java.time.LocalDateTime ahora,
            @Param("estadoPendiente") EstadoTicket estadoPendiente,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Ticket t SET t.estadoTicket = :estadoExpirado " +
           "WHERE t.usuario.idUsuario = :idUsuario " +
           "AND t.evento.idEvento = :idEvento " +
           "AND t.estadoTicket = :estadoPendiente " +
           "AND t.expiraEn IS NOT NULL " +
           "AND t.expiraEn <= :ahora")
    int expirarCheckoutPendienteVencido(
            @Param("idUsuario") Long idUsuario,
            @Param("idEvento") Long idEvento,
            @Param("ahora") java.time.LocalDateTime ahora,
            @Param("estadoPendiente") EstadoTicket estadoPendiente,
            @Param("estadoExpirado") EstadoTicket estadoExpirado);

    @Query("""
    SELECT t FROM Ticket t
    JOIN FETCH t.usuario u
    JOIN FETCH u.acceso
    JOIN FETCH t.evento e
    WHERE e.fechaEvento = :fecha
    AND t.estadoTicket IN :estados
    AND e.estado = com.devops.backend.evento.enums.Estado.ACTIVO
    """)
    List<Ticket> findTicketsActivosParaFecha(
            @Param("fecha") LocalDate fecha,
            @Param("estados") List<EstadoTicket> estados
    );

    @Query("""
    SELECT t FROM Ticket t
    JOIN FETCH t.usuario u
    JOIN FETCH u.acceso
    JOIN FETCH t.evento e
    WHERE e.idEvento = :eventoId
    AND t.estadoTicket IN :estados
""")
    List<Ticket> findTicketsActivosPorEvento(
            @Param("eventoId") Long eventoId,
            @Param("estados") List<EstadoTicket> estados
    );
}
