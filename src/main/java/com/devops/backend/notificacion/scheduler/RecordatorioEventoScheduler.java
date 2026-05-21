package com.devops.backend.notificacion.scheduler;

import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.shared.events.RecordatorioEventoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioEventoScheduler {

    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Se ejecuta todos los días a las 10:00 AM
    @Scheduled(cron = "0 0 10 * * *")
    public void enviarRecordatorios() {
        log.info("[SCHEDULER] Iniciando envío de recordatorios de eventos...");
        int publicados = procesarRecordatorios();
        log.info("[SCHEDULER] Recordatorios publicados: {}", publicados);
    }

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
                        new RecordatorioEventoEvent(this, ticket, ticket.getEvento(), email)
                );
                publicados++;
            }
        }

        return publicados;
    }
}