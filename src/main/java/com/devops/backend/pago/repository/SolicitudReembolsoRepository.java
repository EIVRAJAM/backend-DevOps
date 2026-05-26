package com.devops.backend.pago.repository;

import com.devops.backend.evento.enums.EstadoSolicitudReembolso;
import com.devops.backend.pago.entity.SolicitudReembolso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudReembolsoRepository extends JpaRepository<SolicitudReembolso, Long> {
    
    List<SolicitudReembolso> findByUsuarioSolicitante_IdUsuario(Long idUsuario);
    
    List<SolicitudReembolso> findByTicket_Evento_IdEvento(Long idEvento);
    
    boolean existsByTicket_IdTicketAndEstadoSolicitudNotIn(Long idTicket, List<EstadoSolicitudReembolso> estados);

    long countByEstadoSolicitud(EstadoSolicitudReembolso estadoSolicitud);

}
