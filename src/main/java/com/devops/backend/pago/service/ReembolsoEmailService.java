package com.devops.backend.pago.service;

import com.devops.backend.pago.dto.OrganizerRefundEmailData;
import com.devops.backend.pago.dto.OrganizerRefundEmailPayload;
import com.devops.backend.pago.dto.RefundEmailData;
import com.devops.backend.pago.dto.RefundEmailModel;
import com.devops.backend.shared.email.EmailJobType;
import com.devops.backend.shared.email.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final EmailQueueService emailQueueService;

    public void encolarCorreoSolicitudAprobada(RefundEmailData data) {
        RefundEmailModel model = buildModel(
                "Solicitud de reembolso aprobada",
                "Tu solicitud de reembolso fue aprobada.",
                data,
                "APROBADA",
                "Tu solicitud fue aprobada por el organizador. El reembolso corresponde al 100% del valor pagado. El organizador debera tramitar la devolucion por fuera de la plataforma.",
                "#dcfce7",
                "#166534"
        );

        emailQueueService.enqueueHtmlEmail(
                EmailJobType.REEMBOLSO_APROBADA,
                data.email(),
                model.title() + " - " + model.eventName(),
                "refund-status.mustache",
                model);
    }

    public void encolarCorreoSolicitudRechazada(RefundEmailData data) {
        RefundEmailModel model = buildModel(
                "Solicitud de reembolso rechazada",
                "Tu solicitud de reembolso fue rechazada.",
                data,
                "RECHAZADA",
                "Tu solicitud fue rechazada por el organizador. Puedes revisar el comentario para conocer el motivo de la decision.",
                "#fee2e2",
                "#991b1b"
        );

        emailQueueService.enqueueHtmlEmail(
                EmailJobType.REEMBOLSO_RECHAZADA,
                data.email(),
                model.title() + " - " + model.eventName(),
                "refund-status.mustache",
                model);
    }

    public void encolarCorreoSolicitudReembolsada(RefundEmailData data) {
        RefundEmailModel model = buildModel(
                "Solicitud de reembolso completada",
                "Tu solicitud fue marcada como reembolsada.",
                data,
                "REEMBOLSADA",
                "El organizador marco la solicitud como reembolsada. Esto significa que el tramite externo de devolucion ya fue realizado.",
                "#dbeafe",
                "#1d4ed8"
        );

        emailQueueService.enqueueHtmlEmail(
                EmailJobType.REEMBOLSO_REEMBOLSADA,
                data.email(),
                model.title() + " - " + model.eventName(),
                "refund-status.mustache",
                model);
    }

    public void encolarCorreoSolicitudCreadaUsuario(RefundEmailData data) {
        RefundEmailModel model = buildModel(
                "Solicitud de reembolso enviada",
                "Tu solicitud de reembolso fue enviada al organizador.",
                data,
                "SOLICITADA",
                "Tu solicitud fue enviada al organizador del evento. Recibiras notificaciones cuando haya cambios en el estado de tu solicitud.",
                "#fef3c7",
                "#92400e"
        );

        emailQueueService.enqueueHtmlEmail(
                EmailJobType.REEMBOLSO_SOLICITUD_USUARIO,
                data.email(),
                model.title() + " - " + model.eventName(),
                "refund-status.mustache",
                model);
    }

    public void encolarCorreoNuevaSolicitudOrganizador(OrganizerRefundEmailData data) {
        OrganizerRefundEmailPayload payload = OrganizerRefundEmailPayload.from(data);
        String subject = "Nueva solicitud de reembolso - " + payload.eventoNombre();

        List<org.springframework.web.multipart.MultipartFile> adjuntos = new ArrayList<>();
        if (data.certificadoCuenta() != null && !data.certificadoCuenta().isEmpty()) {
            adjuntos.add(data.certificadoCuenta());
        }
        if (data.documentoAdicional() != null && !data.documentoAdicional().isEmpty()) {
            adjuntos.add(data.documentoAdicional());
        }

        if (!adjuntos.isEmpty()) {
            emailQueueService.enqueueHtmlEmailWithAttachments(
                    EmailJobType.REEMBOLSO_SOLICITUD_ORGANIZADOR,
                    payload.emailOrganizador(),
                    subject,
                    "refund-organizer.mustache",
                    payload,
                    adjuntos.toArray(new org.springframework.web.multipart.MultipartFile[0]));
        } else {
            emailQueueService.enqueueHtmlEmail(
                    EmailJobType.REEMBOLSO_SOLICITUD_ORGANIZADOR,
                    payload.emailOrganizador(),
                    subject,
                    "refund-organizer.mustache",
                    payload);
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
