
package com.devops.backend.notificacion.listener;

import com.devops.backend.evento.service.QrCodeService;
import com.devops.backend.notificacion.service.EmailServiceImp;
import com.devops.backend.notificacion.service.EmailTemplateService;
import com.devops.backend.shared.events.InscripcionConfirmadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InscripcionEmailListener {

    private final EmailServiceImp emailService;
    private final EmailTemplateService templateService;
    private final QrCodeService qrCodeService;

    /**
     * El usuario ya recibió su respuesta 201 antes de que este método siquiera empiece.
     */
    @Async("emailTaskExecutor")
    @EventListener
    public void onInscripcionConfirmada(InscripcionConfirmadaEvent event) {
        log.debug("[EMAIL-LISTENER] Procesando confirmación para ticket #{}", event.getTicket().getIdTicket());

        try {
            byte[] qrBytes = qrCodeService.generarQrPng(event.getTicket().getCodigoQr());

            // 2. Preparar las variables para la plantilla HTML
            Map<String, Object> variables = construirVariables(event);

            // 3. Renderizar el HTML con Thymeleaf
            String htmlContent = templateService.renderizar(
                    "mail/confirmacion-inscripcion",
                    variables
            );

            // 4. Enviar con QR adjunto
            emailService.enviarConAdjunto(
                    event.getEmailDestinatario(),
                    " Tu ticket para " + event.getEvento().getNombreEvento(),
                    htmlContent,
                    qrBytes,
                    "ticket-qr.png"
            );

        } catch (Exception e) {

            log.error("[EMAIL-LISTENER] Falló envío de confirmación para ticket #{}: {}",
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