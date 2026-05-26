package com.devops.backend.pago.repository;

import com.devops.backend.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    boolean existsByStripeEventId(String stripeEventId);

    List<Pago> findByTicket_IdTicket(Long idTicket);

    List<Pago> findByTicket_Usuario_IdUsuario(Long idUsuario);

    List<Pago> findByTicket_Evento_IdEvento(Long idEvento);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p")
    BigDecimal sumAllMontos();

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.ticket.evento.idEvento = :eventoId")
    BigDecimal sumMontosByEventoId(@Param("eventoId") Long eventoId);
}
