package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.CheckinRequestDTO;
import com.devops.backend.evento.dto.CheckinResponseDTO;
import com.devops.backend.evento.dto.CheckinResumenDTO;
import com.devops.backend.evento.dto.EstadoCheckinDTO;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.exception.CheckinYaRealizadoException;
import com.devops.backend.evento.exception.TicketNoValidoParaCheckinException;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.shared.events.CheckinConfirmadoEvent;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final TicketRepository ticketRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoAutorizacionService autorizacionService;
    private final ApplicationEventPublisher eventPublisher;
    @Override
    @Transactional
    public CheckinResponseDTO realizarCheckin(Long eventoId, CheckinRequestDTO request) {
        autorizacionService.validarAccesoOperativo(eventoId);

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        validarEstadoEvento(evento);
        validarVentanaCheckin(evento);

        Ticket ticket = ticketRepository.findByEvento_IdEventoAndCodigoQr(eventoId, request.codigoQr())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún ticket válido con ese código QR para este evento"));

        if (ticket.getEstadoTicket() != EstadoTicket.GRATIS && ticket.getEstadoTicket() != EstadoTicket.PAGADO) {
            throw new TicketNoValidoParaCheckinException("El ticket no es válido para ingreso. Estado actual: " + ticket.getEstadoTicket());
        }

        if (Boolean.TRUE.equals(ticket.getCheckinRealizado())) {
            throw new CheckinYaRealizadoException("El check-in ya fue realizado para este ticket el " + ticket.getFechaCheckin());
        }

        Usuario usuarioScanner = obtenerUsuarioAutenticado();

        ticket.setCheckinRealizado(true);
        ticket.setFechaCheckin(LocalDateTime.now());
        ticket.setUsuarioCheckin(usuarioScanner);

        ticket = ticketRepository.save(ticket);

        Usuario asistente = ticket.getUsuario();
        String nombreAsistente = asistente.getNombres() + " " + asistente.getApellidos();

        String emailAsistente = ticket.getUsuario().getAcceso() != null
                ? ticket.getUsuario().getAcceso().getCorreoAcceso()
                : null;

        if (emailAsistente != null) {
            eventPublisher.publishEvent(
                    new CheckinConfirmadoEvent(this, ticket, ticket.getEvento(), emailAsistente)
            );
        }
        return new CheckinResponseDTO(
                ticket.getIdTicket(),
                evento.getIdEvento(),
                evento.getNombreEvento(),
                nombreAsistente,
                ticket.getEstadoTicket(),
                ticket.getCheckinRealizado(),
                ticket.getFechaCheckin()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CheckinResumenDTO obtenerResumen(Long eventoId) {
        autorizacionService.validarAccesoOperativo(eventoId);

        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento no encontrado");
        }

        List<EstadoTicket> excluidos = List.of(
                EstadoTicket.PENDIENTE,
                EstadoTicket.CANCELADO,
                EstadoTicket.REEMBOLSADO,
                EstadoTicket.EXPIRADO);
        long totalInscritos = ticketRepository.countByEvento_IdEventoAndEstadoTicketNotIn(eventoId, excluidos);
        long totalIngresados = ticketRepository.countByEvento_IdEventoAndCheckinRealizadoTrue(eventoId);
        long totalPendientes = totalInscritos - totalIngresados;
        
        double porcentaje = 0.0;
        if (totalInscritos > 0) {
            porcentaje = ((double) totalIngresados / totalInscritos) * 100.0;
            porcentaje = Math.round(porcentaje * 100.0) / 100.0; // 2 decimales
        }

        return new CheckinResumenDTO(
                totalInscritos,
                totalIngresados,
                totalPendientes,
                porcentaje
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoCheckinDTO obtenerEstadoCheckin(Long eventoId) {
        autorizacionService.validarAccesoOperativo(eventoId);

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        ZoneId zona = ZoneId.of("America/Bogota");
        ZonedDateTime ahora = ZonedDateTime.now(zona);

        LocalDateTime aperturaCheckin = null;
        if (evento.getFechaEvento() != null && evento.getHoraEvento() != null) {
            ZonedDateTime inicioEvento = ZonedDateTime.of(
                    evento.getFechaEvento(), evento.getHoraEvento(), zona);
            aperturaCheckin = inicioEvento.minusHours(1).toLocalDateTime();
        }

        boolean habilitado = true;
        String motivo = "Check-in disponible";

        if (evento.getEstadoEvento() != EstadoEvento.PUBLICADO) {
            habilitado = false;
            motivo = "El evento no esta publicado. Estado: " + evento.getEstadoEvento();
        } else if (evento.getEstado() != Estado.ACTIVO) {
            habilitado = false;
            motivo = "El evento no esta activo. Estado: " + evento.getEstado();
        } else if (aperturaCheckin != null && ahora.isBefore(aperturaCheckin.atZone(zona))) {
            habilitado = false;
            motivo = "El check-in aun no esta habilitado. Se abre 1 hora antes del evento.";
        }

        return new EstadoCheckinDTO(
                habilitado,
                motivo,
                evento.getIdEvento(),
                evento.getNombreEvento(),
                evento.getEstadoEvento().name(),
                evento.getEstado().name(),
                evento.getFechaEvento(),
                evento.getHoraEvento(),
                aperturaCheckin,
                ahora.toLocalDateTime()
        );
    }

    private void validarEstadoEvento(Evento evento) {
        if (evento.getEstadoEvento() != EstadoEvento.PUBLICADO) {
            throw new TicketNoValidoParaCheckinException(
                    "El evento no esta disponible para check-in. Estado: " + evento.getEstadoEvento());
        }
        if (evento.getEstado() != Estado.ACTIVO) {
            throw new TicketNoValidoParaCheckinException(
                    "El evento no esta activo. Estado: " + evento.getEstado());
        }
    }

    private void validarVentanaCheckin(Evento evento) {
        if (evento.getFechaEvento() == null || evento.getHoraEvento() == null) {
            return;
        }

        ZoneId zona = ZoneId.of("America/Bogota");
        ZonedDateTime ahora = ZonedDateTime.now(zona);
        ZonedDateTime inicioEvento = ZonedDateTime.of(
                evento.getFechaEvento(),
                evento.getHoraEvento(),
                zona);
        ZonedDateTime aperturaCheckin = inicioEvento.minusHours(1);

        if (ahora.isBefore(aperturaCheckin)) {
            throw new TicketNoValidoParaCheckinException(
                    "El check-in aun no esta habilitado. Se abre 1 hora antes del evento.");
        }
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        Long idUsuario = Long.parseLong(auth.getName());
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}
