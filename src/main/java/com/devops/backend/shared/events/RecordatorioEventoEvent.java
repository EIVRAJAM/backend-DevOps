package com.devops.backend.shared.events;

import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.Ticket;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RecordatorioEventoEvent extends ApplicationEvent {

    private final Ticket ticket;
    private final Evento evento;
    private final String emailDestinatario;

    public RecordatorioEventoEvent(Object source, Ticket ticket, Evento evento, String emailDestinatario) {
        super(source);
        this.ticket = ticket;
        this.evento = evento;
        this.emailDestinatario = emailDestinatario;
    }
}