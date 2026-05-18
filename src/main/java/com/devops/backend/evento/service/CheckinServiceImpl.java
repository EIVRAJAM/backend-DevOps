package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.CheckinRequestDTO;
import com.devops.backend.evento.dto.CheckinResponseDTO;
import com.devops.backend.evento.dto.CheckinResumenDTO;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.Ticket;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.exception.CheckinYaRealizadoException;
import com.devops.backend.evento.exception.TicketNoValidoParaCheckinException;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final TicketRepository ticketRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoAutorizacionService autorizacionService;

    @Override
    @Transactional
    public CheckinResponseDTO realizarCheckin(Long eventoId, CheckinRequestDTO request) {
        autorizacionService.validarAccesoOperativo(eventoId);

        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento no encontrado");
        }

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

        Evento evento = ticket.getEvento();
        Usuario asistente = ticket.getUsuario();
        String nombreAsistente = asistente.getNombres() + " " + asistente.getApellidos();

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

        List<EstadoTicket> excluidos = List.of(EstadoTicket.PENDIENTE, EstadoTicket.CANCELADO, EstadoTicket.REEMBOLSADO);
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
