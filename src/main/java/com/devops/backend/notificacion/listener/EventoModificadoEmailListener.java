package com.devops.backend.notificacion.listener;

import com.devops.backend.shared.email.enums.EmailJobType;
import com.devops.backend.shared.email.queue.EmailQueueService;
import com.devops.backend.shared.events.EventoModificadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventoModificadoEmailListener {

    private final EmailQueueService emailQueueService;

    @Async("emailTaskExecutor")
    @EventListener
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onEventoModificado(EventoModificadoEvent event) {
        log.debug("[CAMBIO-EVENTO] Encolando notificacion para {}", event.getEmailDestinatario());

        try {
            Map<String, Object> variables = construirVariables(event);

            emailQueueService.enqueueHtmlEmail(
                    EmailJobType.CAMBIO_EVENTO,
                    event.getEmailDestinatario(),
                    "Cambio en el evento: " + event.getEvento().getNombreEvento(),
                    "mail/cambio-evento",
                    variables
            );

        } catch (Exception e) {
            log.error("[CAMBIO-EVENTO] Fallo encolado para ticket #{}: {}",
                    event.getTicket().getIdTicket(), e.getMessage(), e);
        }
    }

    private Map<String, Object> construirVariables(EventoModificadoEvent event) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        Map<String, Object> vars = new HashMap<>();

        vars.put("nombreUsuario", event.getTicket().getUsuario().getNombres());
        vars.put("nombreEvento", event.getEvento().getNombreEvento());
        vars.put("idTicket", event.getTicket().getIdTicket());

        vars.put("fechaAnterior", event.getFechaAnterior() != null
                ? event.getFechaAnterior().format(fmt) : "Sin fecha");
        vars.put("horaAnterior", event.getHoraAnterior() != null
                ? event.getHoraAnterior().toString() : "Sin hora");
        vars.put("lugarAnterior", event.getLugarAnterior() != null
                ? event.getLugarAnterior() : "Sin lugar");

        vars.put("fechaNueva", event.getEvento().getFechaEvento() != null
                ? event.getEvento().getFechaEvento().format(fmt) : "Por confirmar");
        vars.put("horaNueva", event.getEvento().getHoraEvento() != null
                ? event.getEvento().getHoraEvento().toString() : "Por confirmar");
        vars.put("lugarNuevo", event.getEvento().getLugarEvento() != null
                ? event.getEvento().getLugarEvento() : "Por confirmar");

        return vars;
    }
}
