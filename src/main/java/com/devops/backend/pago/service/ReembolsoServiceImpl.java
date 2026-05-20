package com.devops.backend.pago.service;

import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoEvento;
import com.devops.backend.evento.enums.EstadoSolicitudReembolso;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.exception.UnauthorizedException;
import com.devops.backend.pago.dto.*;
import com.devops.backend.pago.entity.DatosReembolsoSolicitud;
import com.devops.backend.pago.entity.SolicitudReembolso;
import com.devops.backend.pago.exception.ReembolsoNoPermitidoException;
import com.devops.backend.pago.exception.SolicitudReembolsoNotFoundException;
import com.devops.backend.pago.repository.SolicitudReembolsoRepository;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReembolsoServiceImpl implements ReembolsoService {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");
    private static final long DIAS_ANTES_EVENTO = 7;

    private final SolicitudReembolsoRepository solicitudRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final ReembolsoEmailService reembolsoEmailService;
    private final ReembolsoRequestValidator validator;

    private static final List<EstadoSolicitudReembolso> ESTADOS_INACTIVOS = Arrays.asList(
            EstadoSolicitudReembolso.RECHAZADA,
            EstadoSolicitudReembolso.CANCELADA,
            EstadoSolicitudReembolso.FALLIDA,
            EstadoSolicitudReembolso.REEMBOLSADA);

    @Override
    public SolicitudReembolsoResponse solicitarReembolso(Long idTicket, Long idUsuarioAuth,
            CrearSolicitudReembolsoRequest request) {
        validator.validar(request);

        Ticket ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado"));

        if (!ticket.getUsuario().getIdUsuario().equals(idUsuarioAuth)) {
            throw new UnauthorizedException("No tienes permiso para solicitar reembolso de este ticket");
        }

        if (ticket.getEstadoTicket() != EstadoTicket.PAGADO) {
            throw new ReembolsoNoPermitidoException("Solo se pueden reembolsar tickets PAGADOS");
        }

        if (Boolean.TRUE.equals(ticket.getCheckinRealizado())) {
            throw new ReembolsoNoPermitidoException("No se puede solicitar reembolso de un ticket con check-in");
        }

        Evento evento = ticket.getEvento();

        if (evento.getEstadoEvento() != EstadoEvento.CANCELADO) {
            ZonedDateTime fechaInicioEvento = ZonedDateTime.of(
                    evento.getFechaEvento(),
                    evento.getHoraEvento() != null ? evento.getHoraEvento() : LocalTime.MIDNIGHT,
                    ZONA_HORARIA);
            ZonedDateTime ahora = ZonedDateTime.now(ZONA_HORARIA);

            if (ahora.plusDays(DIAS_ANTES_EVENTO).isAfter(fechaInicioEvento)) {
                throw new ReembolsoNoPermitidoException(
                        "La solicitud debe hacerse al menos " + DIAS_ANTES_EVENTO
                                + " dias antes del evento");
            }
        }

        if (solicitudRepository.existsByTicket_IdTicketAndEstadoSolicitudNotIn(idTicket, ESTADOS_INACTIVOS)) {
            throw new ReembolsoNoPermitidoException("Ya existe una solicitud de reembolso activa para este ticket");
        }

        Usuario usuario = usuarioRepository.findById(idUsuarioAuth)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SolicitudReembolso solicitud = new SolicitudReembolso();
        solicitud.setTicket(ticket);
        solicitud.setUsuarioSolicitante(usuario);
        solicitud.setEstadoSolicitud(EstadoSolicitudReembolso.SOLICITADA);
        solicitud.setMotivoSolicitud(request.getMotivoSolicitud());
        solicitud.setMontoSolicitado(ticket.getMontoPagado());

        DatosReembolsoSolicitud datos = new DatosReembolsoSolicitud();
        datos.setSolicitudReembolso(solicitud);
        datos.setMedioReembolso(request.getMedioReembolso());
        datos.setTitularCuenta(request.getTitularCuenta());
        datos.setDocumentoTitular(request.getDocumentoTitular());
        datos.setEntidadFinanciera(request.getEntidadFinanciera());
        datos.setTipoCuenta(request.getTipoCuenta());
        datos.setNumeroCuentaEnmascarado(enmascararNumeroCuenta(request.getNumeroCuenta()));
        datos.setCorreoContacto(request.getCorreoContacto());
        datos.setTelefonoContacto(request.getTelefonoContacto());
        datos.setObservaciones(request.getObservaciones());
        datos.setCertificadoEnviado(request.getCertificadoCuenta() != null && !request.getCertificadoCuenta().isEmpty());
        datos.setDocumentoAdicionalEnviado(request.getDocumentoAdicional() != null && !request.getDocumentoAdicional().isEmpty());

        solicitud.setDatosReembolso(datos);

        solicitudRepository.save(solicitud);

        encolarCorreos(solicitud, request);

        return mapToResponse(solicitud);
    }

    @Override
    public void cancelarSolicitud(Long idSolicitud, Long idUsuarioAuth) {
        SolicitudReembolso solicitud = getSolicitud(idSolicitud);

        if (!solicitud.getUsuarioSolicitante().getIdUsuario().equals(idUsuarioAuth)) {
            throw new UnauthorizedException("No tienes permiso para cancelar esta solicitud");
        }

        if (solicitud.getEstadoSolicitud() != EstadoSolicitudReembolso.SOLICITADA &&
                solicitud.getEstadoSolicitud() != EstadoSolicitudReembolso.EN_REVISION) {
            throw new ReembolsoNoPermitidoException(
                    "No se puede cancelar en este estado: " + solicitud.getEstadoSolicitud());
        }

        solicitud.setEstadoSolicitud(EstadoSolicitudReembolso.CANCELADA);
        solicitudRepository.save(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudReembolsoResponse> obtenerMisSolicitudes(Long idUsuarioAuth) {
        return solicitudRepository.findByUsuarioSolicitante_IdUsuario(idUsuarioAuth)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudReembolsoResponse obtenerDetalleSolicitud(Long idSolicitud, Long idUsuarioAuth, boolean isAdmin) {
        SolicitudReembolso solicitud = getSolicitud(idSolicitud);

        boolean isOwner = solicitud.getUsuarioSolicitante().getIdUsuario().equals(idUsuarioAuth);
        boolean isOrganizer = isOrganizerOfEvent(solicitud.getTicket().getEvento().getIdEvento(), idUsuarioAuth);

        if (!isAdmin && !isOwner && !isOrganizer) {
            throw new UnauthorizedException("No tienes permisos para ver esta solicitud");
        }

        return mapToResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudReembolsoResponse> listarSolicitudesPorEvento(Long idEvento, Long idUsuarioAuth,
            boolean isAdmin) {
        if (!isAdmin && !isOrganizerOfEvent(idEvento, idUsuarioAuth)) {
            throw new UnauthorizedException("No tienes permisos para ver las solicitudes de este evento");
        }
        return solicitudRepository.findByTicket_Evento_IdEvento(idEvento)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public SolicitudReembolsoResponse revisarSolicitud(Long idEvento, Long idSolicitud, Long idUsuarioAuth,
            boolean isAdmin) {
        if (!isAdmin && !isOrganizerOfEvent(idEvento, idUsuarioAuth)) {
            throw new UnauthorizedException("No tienes permisos para gestionar solicitudes de este evento");
        }

        SolicitudReembolso solicitud = getSolicitud(idSolicitud);

        if (!solicitud.getTicket().getEvento().getIdEvento().equals(idEvento)) {
            throw new UnauthorizedException("La solicitud no pertenece a este evento");
        }

        if (solicitud.getEstadoSolicitud() != EstadoSolicitudReembolso.SOLICITADA) {
            throw new ReembolsoNoPermitidoException(
                        "La solicitud no esta en estado SOLICITADA. Estado actual: "
                                + solicitud.getEstadoSolicitud());
        }

        Usuario responsable = usuarioRepository.findById(idUsuarioAuth).orElse(null);
        solicitud.setEstadoSolicitud(EstadoSolicitudReembolso.EN_REVISION);
        solicitud.setUsuarioResponsable(responsable);
        solicitud.setFechaRevision(LocalDateTime.now());

        solicitudRepository.save(solicitud);
        return mapToResponse(solicitud);
    }

    @Override
    public SolicitudReembolsoResponse aprobarSolicitud(Long idEvento, Long idSolicitud, Long idUsuarioAuth,
            boolean isAdmin, AprobarReembolsoRequest request) {
        if (!isAdmin && !isOrganizerOfEvent(idEvento, idUsuarioAuth)) {
            throw new UnauthorizedException("No tienes permisos para gestionar solicitudes de este evento");
        }

        SolicitudReembolso solicitud = getSolicitud(idSolicitud);

        if (!solicitud.getTicket().getEvento().getIdEvento().equals(idEvento)) {
            throw new UnauthorizedException("La solicitud no pertenece a este evento");
        }

        if (solicitud.getEstadoSolicitud() != EstadoSolicitudReembolso.EN_REVISION &&
                solicitud.getEstadoSolicitud() != EstadoSolicitudReembolso.SOLICITADA) {
            throw new ReembolsoNoPermitidoException(
                        "No se puede aprobar en el estado actual: " + solicitud.getEstadoSolicitud());
        }

        Usuario responsable = usuarioRepository.findById(idUsuarioAuth).orElse(null);
        solicitud.setEstadoSolicitud(EstadoSolicitudReembolso.APROBADA);
        solicitud.setUsuarioResponsable(responsable);
        solicitud.setRespuestaOrganizador(request.getComentario());
        solicitud.setMontoAprobado(solicitud.getMontoSolicitado());

        solicitudRepository.save(solicitud);

        encolarCorreo(solicitud, "APROBADA");

        return mapToResponse(solicitud);
    }

    @Override
    public SolicitudReembolsoResponse rechazarSolicitud(Long idEvento, Long idSolicitud, Long idUsuarioAuth,
            boolean isAdmin, RechazarReembolsoRequest request) {
        if (!isAdmin && !isOrganizerOfEvent(idEvento, idUsuarioAuth)) {
            throw new UnauthorizedException("No tienes permisos para gestionar solicitudes de este evento");
        }

        SolicitudReembolso solicitud = getSolicitud(idSolicitud);

        if (!solicitud.getTicket().getEvento().getIdEvento().equals(idEvento)) {
            throw new UnauthorizedException("La solicitud no pertenece a este evento");
        }

        if (solicitud.getEstadoSolicitud() != EstadoSolicitudReembolso.EN_REVISION &&
                solicitud.getEstadoSolicitud() != EstadoSolicitudReembolso.SOLICITADA) {
            throw new ReembolsoNoPermitidoException(
                        "No se puede rechazar en el estado actual: " + solicitud.getEstadoSolicitud());
        }

        Usuario responsable = usuarioRepository.findById(idUsuarioAuth).orElse(null);
        solicitud.setEstadoSolicitud(EstadoSolicitudReembolso.RECHAZADA);
        solicitud.setUsuarioResponsable(responsable);
        solicitud.setRespuestaOrganizador(request.getComentario());

        solicitudRepository.save(solicitud);

        encolarCorreo(solicitud, "RECHAZADA");

        return mapToResponse(solicitud);
    }

    @Override
    public SolicitudReembolsoResponse marcarReembolsado(Long idEvento, Long idSolicitud, Long idUsuarioAuth,
            boolean isAdmin) {
        if (!isAdmin && !isOrganizerOfEvent(idEvento, idUsuarioAuth)) {
            throw new UnauthorizedException("No tienes permisos para gestionar solicitudes de este evento");
        }

        SolicitudReembolso solicitud = getSolicitud(idSolicitud);

        if (!solicitud.getTicket().getEvento().getIdEvento().equals(idEvento)) {
            throw new UnauthorizedException("La solicitud no pertenece a este evento");
        }

        if (solicitud.getEstadoSolicitud() != EstadoSolicitudReembolso.APROBADA) {
            throw new ReembolsoNoPermitidoException(
                        "Solo se pueden marcar como reembolsadas solicitudes APROBADAS. Estado actual: "
                                + solicitud.getEstadoSolicitud());
        }

        Ticket ticket = solicitud.getTicket();

        ticket.setEstadoTicket(EstadoTicket.REEMBOLSADO);
        ticketRepository.save(ticket);

        ticketRepository.incrementarCupo(ticket.getEvento().getIdEvento());

        solicitud.setEstadoSolicitud(EstadoSolicitudReembolso.REEMBOLSADA);
        solicitud.setFechaProcesamiento(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        encolarCorreo(solicitud, "REEMBOLSADA");

        return mapToResponse(solicitud);
    }

    private SolicitudReembolso getSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new SolicitudReembolsoNotFoundException("Solicitud de reembolso no encontrada"));
    }

    private boolean isOrganizerOfEvent(Long idEvento, Long idUsuario) {
        return eventoRepository.findById(idEvento)
                .map(evento -> evento.getUsuarioCreador().getIdUsuario().equals(idUsuario))
                .orElse(false);
    }

    private void encolarCorreo(SolicitudReembolso solicitud, String accion) {
        RefundEmailData data = extractRefundEmailData(solicitud);

        switch (accion) {
            case "APROBADA" -> reembolsoEmailService.encolarCorreoSolicitudAprobada(data);
            case "RECHAZADA" -> reembolsoEmailService.encolarCorreoSolicitudRechazada(data);
            case "REEMBOLSADA" -> reembolsoEmailService.encolarCorreoSolicitudReembolsada(data);
        }
    }

    private void encolarCorreos(SolicitudReembolso solicitud, CrearSolicitudReembolsoRequest request) {
        RefundEmailData data = extractRefundEmailData(solicitud);
        OrganizerRefundEmailData organizerData = extractOrganizerData(solicitud, request);

        reembolsoEmailService.encolarCorreoSolicitudCreadaUsuario(data);
        reembolsoEmailService.encolarCorreoNuevaSolicitudOrganizador(organizerData);
    }

    private RefundEmailData extractRefundEmailData(SolicitudReembolso solicitud) {
        return new RefundEmailData(
                solicitud.getUsuarioSolicitante().getAcceso().getCorreoAcceso(),
                solicitud.getTicket().getEvento().getNombreEvento(),
                solicitud.getMontoSolicitado(),
                solicitud.getRespuestaOrganizador(),
                solicitud.getIdSolicitudReembolso()
        );
    }

    private OrganizerRefundEmailData extractOrganizerData(SolicitudReembolso solicitud,
            CrearSolicitudReembolsoRequest request) {
        Usuario usuario = solicitud.getUsuarioSolicitante();
        Evento evento = solicitud.getTicket().getEvento();
        String emailOrganizador = evento.getUsuarioCreador().getAcceso().getCorreoAcceso();

        return new OrganizerRefundEmailData(
                emailOrganizador,
                evento.getNombreEvento(),
                usuario.getNombres() + " " + usuario.getApellidos(),
                usuario.getAcceso() != null ? usuario.getAcceso().getCorreoAcceso() : "",
                solicitud.getMontoSolicitado(),
                solicitud.getMotivoSolicitud(),
                request.getMedioReembolso() != null ? request.getMedioReembolso().name() : "",
                request.getTitularCuenta(),
                request.getDocumentoTitular(),
                request.getEntidadFinanciera(),
                request.getTipoCuenta() != null ? request.getTipoCuenta().name() : null,
                request.getNumeroCuenta(),
                request.getCorreoContacto(),
                request.getTelefonoContacto(),
                request.getObservaciones(),
                solicitud.getIdSolicitudReembolso(),
                request.getCertificadoCuenta(),
                request.getDocumentoAdicional()
        );
    }

    private String enmascararNumeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.length() <= 4) {
            return numeroCuenta != null ? numeroCuenta : "";
        }
        String ultimos4 = numeroCuenta.substring(numeroCuenta.length() - 4);
        return "****" + ultimos4;
    }

    private SolicitudReembolsoResponse mapToResponse(SolicitudReembolso s) {
        DatosReembolsoResponse datosResponse = null;
        if (s.getDatosReembolso() != null) {
            DatosReembolsoSolicitud d = s.getDatosReembolso();
            datosResponse = new DatosReembolsoResponse(
                    d.getMedioReembolso() != null ? d.getMedioReembolso().name() : null,
                    d.getTitularCuenta(),
                    d.getDocumentoTitular(),
                    d.getEntidadFinanciera(),
                    d.getTipoCuenta() != null ? d.getTipoCuenta().name() : null,
                    d.getNumeroCuentaEnmascarado(),
                    d.getCorreoContacto(),
                    d.getTelefonoContacto(),
                    d.getObservaciones()
            );
        }

        return SolicitudReembolsoResponse.builder()
                .idSolicitud(s.getIdSolicitudReembolso())
                .idTicket(s.getTicket().getIdTicket())
                .idEvento(s.getTicket().getEvento().getIdEvento())
                .idUsuarioSolicitante(s.getUsuarioSolicitante().getIdUsuario())
                .estado(s.getEstadoSolicitud())
                .motivo(s.getMotivoSolicitud())
                .respuestaOrganizador(s.getRespuestaOrganizador())
                .montoSolicitado(s.getMontoSolicitado())
                .montoAprobado(s.getMontoAprobado())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaRevision(s.getFechaRevision())
                .fechaProcesamiento(s.getFechaProcesamiento())
                .datosReembolso(datosResponse)
                .build();
    }
}
