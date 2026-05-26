package com.devops.backend.notificacion.service;

import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.shared.events.RecordatorioEventoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordatorioService {

    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public int procesarRecordatorios() {
        LocalDate manana = LocalDate.now().plusDays(1);

        List<Ticket> tickets = ticketRepository.findTicketsActivosParaFecha(
                manana, List.of(EstadoTicket.GRATIS, EstadoTicket.PAGADO));

        int publicados = 0;

        for (Ticket ticket : tickets) {
            String email = ticket.getUsuario().getAcceso() != null
                    ? ticket.getUsuario().getAcceso().getCorreoAcceso()
                    : null;

            if (email != null) {
                eventPublisher.publishEvent(
                        new RecordatorioEventoEvent(this, ticket, ticket.getEvento(), email));
                publicados++;
            } else {
                log.info("[RECORDATORIO] Usuario sin correo para ticket #{} - omitido",
                        ticket.getIdTicket());
            }
        }

        log.info("[RECORDATORIO] {} recordatorios publicados de {} tickets validos",
                publicados, tickets.size());
        return publicados;
    }
}
