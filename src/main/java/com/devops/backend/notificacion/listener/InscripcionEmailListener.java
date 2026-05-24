package com.devops.backend.notificacion.listener;

import com.devops.backend.evento.service.QrCodeService;
import com.devops.backend.shared.email.enums.EmailJobType;
import com.devops.backend.shared.email.queue.EmailQueueService;
import com.devops.backend.shared.events.InscripcionConfirmadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class InscripcionEmailListener {

    private final EmailQueueService emailQueueService;
    private final QrCodeService qrCodeService;

    @Async("emailTaskExecutor")
    @EventListener
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onInscripcionConfirmada(InscripcionConfirmadaEvent event) {
        log.debug("[EMAIL-LISTENER] Encolando confirmacion para ticket #{}", event.getTicket().getIdTicket());

        try {
            byte[] qrBytes = qrCodeService.generarQrPng(event.getTicket().getCodigoQr());

            Map<String, Object> variables = construirVariables(event);

            emailQueueService.enqueueHtmlEmailWithByteAttachment(
                    EmailJobType.INSCRIPCION,
                    event.getEmailDestinatario(),
                    "Tu ticket para " + event.getEvento().getNombreEvento(),
                    "mail/confirmacion-inscripcion",
                    variables,
                    qrBytes,
                    "ticket-qr.png",
                    "image/png"
            );

        } catch (Exception e) {
            log.error("[EMAIL-LISTENER] Fallo encolado de confirmacion para ticket #{}: {}",
                    event.getTicket().getIdTicket(), e.getMessage(), e);
        }
    }

    private Map<String, Object> construirVariables(InscripcionConfirmadaEvent event) {
        Map<String, Object> vars = new HashMap<>();

        String nombreUsuario = event.getTicket().getUsuario().getNombres();
        vars.put("nombreUsuario", nombreUsuario);

        vars.put("nombreEvento", event.getEvento().getNombreEvento());
        vars.put("idTicket", event.getTicket().getIdTicket());
        vars.put("codigoQr", event.getTicket().getCodigoQr());

        if (event.getEvento().getFechaEvento() != null) {
            String fechaFormateada = event.getEvento().getFechaEvento()
                    .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es", "CO")));
            vars.put("fechaEvento", fechaFormateada);
        } else {
            vars.put("fechaEvento", "Por confirmar");
        }

        vars.put("lugarEvento", event.getEvento().getLugarEvento() != null
                ? event.getEvento().getLugarEvento()
                : "Por confirmar");

        return vars;
    }
}
