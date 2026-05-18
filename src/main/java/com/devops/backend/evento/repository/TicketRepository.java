package com.devops.backend.evento.repository;

import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByUsuario_IdUsuarioAndEvento_IdEvento(Long idUsuario, Long idEvento);

    Optional<Ticket> findByUsuario_IdUsuarioAndEvento_IdEvento(Long idUsuario, Long idEvento);

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

    Optional<Ticket> findByEvento_IdEventoAndCodigoQr(Long idEvento, String codigoQr);

    long countByEvento_IdEventoAndEstadoTicketNotIn(Long idEvento, List<EstadoTicket> excluidos);

    long countByEvento_IdEventoAndCheckinRealizadoTrue(Long idEvento);
}
