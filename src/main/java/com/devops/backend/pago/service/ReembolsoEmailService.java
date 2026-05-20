package com.devops.backend.pago.service;

import com.devops.backend.pago.dto.OrganizerRefundEmailData;
import com.devops.backend.pago.dto.RefundEmailData;
import com.devops.backend.pago.dto.RefundEmailModel;
import com.devops.backend.shared.email.EmailSenderService;
import com.devops.backend.shared.email.EmailTemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReembolsoEmailService {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm", LOCALE_CO);

    private final EmailTemplateRenderer templateRenderer;
    private final EmailSenderService emailSenderService;

    public void enviarCorreoSolicitudAprobada(RefundEmailData data) {
        RefundEmailModel model = buildModel(
                "Solicitud de reembolso aprobada",
                "Tu solicitud de reembolso fue aprobada.",
                data,
                "APROBADA",
                "Tu solicitud fue aprobada por el organizador. El reembolso corresponde al 100% del valor pagado. El organizador debera tramitar la devolucion por fuera de la plataforma.",
                "#dcfce7",
                "#166534"
        );

        send(data.email(), model);
    }

    public void enviarCorreoSolicitudRechazada(RefundEmailData data) {
        RefundEmailModel model = buildModel(
                "Solicitud de reembolso rechazada",
                "Tu solicitud de reembolso fue rechazada.",
                data,
                "RECHAZADA",
                "Tu solicitud fue rechazada por el organizador. Puedes revisar el comentario para conocer el motivo de la decision.",
                "#fee2e2",
                "#991b1b"
        );

        send(data.email(), model);
    }

    public void enviarCorreoSolicitudReembolsada(RefundEmailData data) {
        RefundEmailModel model = buildModel(
                "Solicitud de reembolso completada",
                "Tu solicitud fue marcada como reembolsada.",
                data,
                "REEMBOLSADA",
                "El organizador marco la solicitud como reembolsada. Esto significa que el tramite externo de devolucion ya fue realizado.",
                "#dbeafe",
                "#1d4ed8"
        );

        send(data.email(), model);
    }

    public void enviarCorreoSolicitudCreadaUsuario(RefundEmailData data) {
        RefundEmailModel model = buildModel(
                "Solicitud de reembolso enviada",
                "Tu solicitud de reembolso fue enviada al organizador.",
                data,
                "SOLICITADA",
                "Tu solicitud fue enviada al organizador del evento. Recibiras notificaciones cuando haya cambios en el estado de tu solicitud.",
                "#fef3c7",
                "#92400e"
        );

        send(data.email(), model);
    }

    public void enviarCorreoNuevaSolicitudOrganizador(OrganizerRefundEmailData data) {
        String html = templateRenderer.render("refund-organizer.mustache", data);

        List<MultipartFile> adjuntos = new ArrayList<>();
        if (data.certificadoCuenta() != null && !data.certificadoCuenta().isEmpty()) {
            adjuntos.add(data.certificadoCuenta());
        }
        if (data.documentoAdicional() != null && !data.documentoAdicional().isEmpty()) {
            adjuntos.add(data.documentoAdicional());
        }

        MultipartFile[] attachments = adjuntos.toArray(new MultipartFile[0]);

        try {
            if (attachments.length > 0) {
                emailSenderService.sendHtmlEmailWithAttachments(
                        data.emailOrganizador(),
                        "Nueva solicitud de reembolso - " + data.eventoNombre(),
                        html,
                        attachments);
            } else {
                emailSenderService.sendHtmlEmail(
                        data.emailOrganizador(),
                        "Nueva solicitud de reembolso - " + data.eventoNombre(),
                        html);
            }
            log.info("Correo de nueva solicitud enviado al organizador. solicitudId={}, organizador={}",
                    data.idSolicitud(), data.emailOrganizador());
        } catch (Exception ex) {
            log.error("No se pudo enviar correo al organizador. solicitudId={}", data.idSolicitud(), ex);
        }
    }

    private RefundEmailModel buildModel(
            String title,
            String preheader,
            RefundEmailData data,
            String status,
            String mainMessage,
            String badgeBg,
            String badgeColor
    ) {
        String amount = formatMoney(data.monto());
        String date = formatDate(LocalDateTime.now());

        return new RefundEmailModel(
                title,
                preheader,
                data.eventoNombre(),
                amount,
                status,
                date,
                mainMessage,
                data.comentarioOrganizador(),
                badgeBg,
                badgeColor
        );
    }

    private void send(String email, RefundEmailModel model) {
        String subject = model.title() + " - " + model.eventName();

        try {
            String html = templateRenderer.render("refund-status.mustache", model);
            emailSenderService.sendHtmlEmail(email, subject, html);
            log.info("Correo de reembolso enviado. estado={}, destinatario={}", model.status(), email);
        } catch (Exception ex) {
            log.error("No se pudo enviar correo de reembolso. estado={}, destinatario={}",
                    model.status(), email, ex);
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "$ 0,00";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_CO);
        return formatter.format(amount);
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "No disponible";
        }

        return dateTime.format(DATE_FORMATTER);
    }
}
