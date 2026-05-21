package com.devops.backend.notificacion.listener;

import com.devops.backend.notificacion.service.EmailServiceImp;
import com.devops.backend.notificacion.service.EmailTemplateService;
import com.devops.backend.shared.events.RecordatorioEventoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioEmailListener {

    private final EmailServiceImp emailService;
    private final EmailTemplateService templateService;

    @Async("emailTaskExecutor")
    @EventListener
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onRecordatorioEvento(RecordatorioEventoEvent event) {
        log.debug("[RECORDATORIO] Enviando recordatorio para ticket #{}", event.getTicket().getIdTicket());

        try {
            Map<String, Object> variables = construirVariables(event);

            String htmlContent = templateService.renderizar(
                    "mail/recordatorio-evento",
                    variables
            );

            emailService.enviarConAdjunto(
                    event.getEmailDestinatario(),
                    "Recordatorio: " + event.getEvento().getNombreEvento() + " es mañana",
                    htmlContent,
                    null,
                    null
            );

            log.info("[RECORDATORIO] Correo enviado a {} para evento #{}",
                    event.getEmailDestinatario(), event.getEvento().getIdEvento());

        } catch (Exception e) {
            log.error("[RECORDATORIO] Falló envío para ticket #{}: {}",
                    event.getTicket().getIdTicket(), e.getMessage(), e);
        }
    }

    private Map<String, Object> construirVariables(RecordatorioEventoEvent event) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("nombreUsuario", event.getTicket().getUsuario().getNombres());
        vars.put("nombreEvento", event.getEvento().getNombreEvento());
        vars.put("idTicket", event.getTicket().getIdTicket());

        if (event.getEvento().getFechaEvento() != null) {
            vars.put("fechaEvento", event.getEvento().getFechaEvento()
                    .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "CO"))));
        } else {
            vars.put("fechaEvento", "Por confirmar");
        }

        vars.put("horaEvento", event.getEvento().getHoraEvento() != null
                ? event.getEvento().getHoraEvento().toString()
                : "Por confirmar");

        vars.put("lugarEvento", event.getEvento().getLugarEvento() != null
                ? event.getEvento().getLugarEvento()
                : "Por confirmar");

        return vars;
    }
}