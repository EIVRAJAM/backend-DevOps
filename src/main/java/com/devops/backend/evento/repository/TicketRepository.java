package com.devops.backend.evento.repository;

import com.devops.backend.evento.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    
    boolean existsByUsuario_IdUsuarioAndEvento_IdEvento(Long idUsuario, Long idEvento);

    Optional<Ticket> findByUsuario_IdUsuarioAndEvento_IdEvento(Long idUsuario, Long idEvento);
}
