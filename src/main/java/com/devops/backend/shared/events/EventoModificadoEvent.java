package com.devops.backend.shared.events;

import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.Ticket;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class EventoModificadoEvent extends ApplicationEvent {

    private final Ticket ticket;
    private final Evento evento;
    private final String emailDestinatario;

    // Valores anteriores para mostrar el cambio en el correo
    private final LocalDate fechaAnterior;
    private final LocalTime horaAnterior;
    private final String lugarAnterior;

    public EventoModificadoEvent(Object source, Ticket ticket, Evento evento,
                                 String emailDestinatario, LocalDate fechaAnterior,
                                 LocalTime horaAnterior, String lugarAnterior) {
        super(source);
        this.ticket = ticket;
        this.evento = evento;
        this.emailDestinatario = emailDestinatario;
        this.fechaAnterior = fechaAnterior;
        this.horaAnterior = horaAnterior;
        this.lugarAnterior = lugarAnterior;
    }
}