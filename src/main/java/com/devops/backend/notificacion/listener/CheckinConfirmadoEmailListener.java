package com.devops.backend.notificacion.listener;
import com.devops.backend.notificacion.service.EmailServiceImp;
import com.devops.backend.notificacion.service.EmailTemplateService;
import com.devops.backend.shared.events.CheckinConfirmadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CheckinConfirmadoEmailListener {

    private final EmailServiceImp emailService;
    private final EmailTemplateService templateService;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onCheckinConfirmado(CheckinConfirmadoEvent event) {
        log.debug("[CHECKIN] Enviando confirmación para ticket #{}", event.getTicket().getIdTicket());

        try {
            Map<String, Object> variables = construirVariables(event);

            String htmlContent = templateService.renderizar(
                    "mail/checkin-confirmado",
                    variables
            );

            emailService.enviarConAdjunto(
                    event.getEmailDestinatario(),
                    "¡Bienvenido! Ingreso confirmado - " + event.getEvento().getNombreEvento(),
                    htmlContent,
                    null,
                    null
            );

            log.info("[CHECKIN] Correo enviado a {} para evento #{}",
                    event.getEmailDestinatario(), event.getEvento().getIdEvento());

        } catch (Exception e) {
            log.error("[CHECKIN] Falló envío para ticket #{}: {}",
                    event.getTicket().getIdTicket(), e.getMessage(), e);
        }
    }

    private Map<String, Object> construirVariables(CheckinConfirmadoEvent event) {
        Locale esCO = new Locale("es", "CO");
        Map<String, Object> vars = new HashMap<>();

        vars.put("nombreUsuario", event.getTicket().getUsuario().getNombres());
        vars.put("nombreEvento",  event.getEvento().getNombreEvento());
        vars.put("idTicket",      event.getTicket().getIdTicket());

        vars.put("fechaCheckin", event.getTicket().getFechaCheckin()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy, hh:mm a", esCO)));

        vars.put("fechaEvento", event.getEvento().getFechaEvento() != null
                ? event.getEvento().getFechaEvento()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", esCO))
                : "Por confirmar");

        vars.put("lugarEvento", event.getEvento().getLugarEvento() != null
                ? event.getEvento().getLugarEvento()
                : "Por confirmar");

        return vars;
    }
}