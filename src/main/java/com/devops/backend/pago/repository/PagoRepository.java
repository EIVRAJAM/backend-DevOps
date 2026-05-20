package com.devops.backend.pago.repository;

import com.devops.backend.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    boolean existsByStripeEventId(String stripeEventId);
    
    java.util.List<Pago> findByTicket_IdTicket(Long idTicket);
    
    java.util.List<Pago> findByTicket_Usuario_IdUsuario(Long idUsuario);
    
    java.util.List<Pago> findByTicket_Evento_IdEvento(Long idEvento);
}
