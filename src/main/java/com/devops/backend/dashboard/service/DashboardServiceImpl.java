package com.devops.backend.dashboard.service;

import com.devops.backend.dashboard.dto.*;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import com.devops.backend.evento.enums.EstadoSolicitudReembolso;
import com.devops.backend.evento.enums.EstadoTicket;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.pago.entity.SolicitudReembolso;
import com.devops.backend.pago.repository.PagoRepository;
import com.devops.backend.pago.repository.SolicitudReembolsoRepository;
import com.devops.backend.sesion.repository.SesionRepository;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final TicketRepository ticketRepository;
    private final PagoRepository pagoRepository;
    private final SesionRepository sesionRepository;
    private final SolicitudReembolsoRepository solicitudReembolsoRepository;

    public DashboardServiceImpl(UsuarioRepository usuarioRepository,
                                EventoRepository eventoRepository,
                                TicketRepository ticketRepository,
                                PagoRepository pagoRepository,
                                SesionRepository sesionRepository,
                                SolicitudReembolsoRepository solicitudReembolsoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.ticketRepository = ticketRepository;
        this.pagoRepository = pagoRepository;
        this.sesionRepository = sesionRepository;
        this.solicitudReembolsoRepository = solicitudReembolsoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalUsuarios = usuarioRepository.count();
        long usuariosActivos = usuarioRepository.countByEstado("ACTIVO");
        long totalEventos = eventoRepository.count();
        long eventosPublicados = eventoRepository.countByEstadoEvento(EstadoEvento.PUBLICADO);
        long totalTickets = ticketRepository.count();

        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = LocalDate.now().atTime(LocalTime.MAX);
        long ticketsVendidosHoy = ticketRepository.countByFechaCompraBetween(hoyInicio, hoyFin);

        long totalPagos = pagoRepository.count();
        BigDecimal montoTotalPagos = pagoRepository.sumAllMontos();
        long reembolsosPendientes = solicitudReembolsoRepository.countByEstadoSolicitud(EstadoSolicitudReembolso.SOLICITADA);
        long sesionesActivas = sesionRepository.countByActivaTrue();

        List<UltimoUsuarioItem> ultimosUsuarios = usuarioRepository.findTop5ByOrderByCreadoEnDesc()
                .stream()
                .map(this::toUltimoUsuarioItem)
                .toList();

        List<ProximoEventoItem> proximosEventos = eventoRepository
                .findTop5ByEstadoEventoAndEstadoOrderByFechaEventoAsc(EstadoEvento.PUBLICADO, Estado.ACTIVO)
                .stream()
                .map(this::toProximoEventoItem)
                .toList();

        return new DashboardStatsResponse(
                totalUsuarios,
                usuariosActivos,
                totalEventos,
                eventosPublicados,
                totalTickets,
                ticketsVendidosHoy,
                totalPagos,
                montoTotalPagos,
                reembolsosPendientes,
                sesionesActivas,
                ultimosUsuarios,
                proximosEventos
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EventoFinanzasResponse getFinanzasByEvento(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento con ID " + eventoId + " no encontrado"));

        List<EstadoTicket> excluidos = List.of(
                EstadoTicket.PENDIENTE, EstadoTicket.CANCELADO,
                EstadoTicket.REEMBOLSADO, EstadoTicket.EXPIRADO);
        long ticketsVendidos = ticketRepository.countByEvento_IdEventoAndEstadoTicketNotIn(eventoId, excluidos);

        long ticketsGratis = ticketRepository.countByEvento_IdEventoAndEstadoTicket(eventoId, EstadoTicket.GRATIS);
        long ticketsPagados = ticketRepository.countByEvento_IdEventoAndEstadoTicket(eventoId, EstadoTicket.PAGADO);

        BigDecimal ingresosTotales = pagoRepository.sumMontosByEventoId(eventoId);

        List<SolicitudReembolso> reembolsos = solicitudReembolsoRepository
                .findByTicket_Evento_IdEvento(eventoId).stream()
                .filter(r -> r.getEstadoSolicitud() == EstadoSolicitudReembolso.SOLICITADA)
                .toList();
        long reembolsosSolicitados = reembolsos.size();
        BigDecimal montoReembolsosPendientes = reembolsos.stream()
                .map(SolicitudReembolso::getMontoSolicitado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double tasaOcupacion = 0;
        if (evento.getCapacidadMaxima() != null && evento.getCapacidadMaxima() > 0) {
            tasaOcupacion = (double) ticketsVendidos / evento.getCapacidadMaxima() * 100;
        }

        return new EventoFinanzasResponse(
                evento.getIdEvento(),
                evento.getNombreEvento(),
                ticketsVendidos,
                ticketsGratis,
                ticketsPagados,
                ingresosTotales,
                reembolsosSolicitados,
                montoReembolsosPendientes,
                Math.round(tasaOcupacion * 10.0) / 10.0
        );
    }

    private UltimoUsuarioItem toUltimoUsuarioItem(Usuario u) {
        return new UltimoUsuarioItem(
                u.getIdUsuario(),
                u.getNombres(),
                u.getApellidos(),
                u.getCreadoEn() != null ? u.getCreadoEn().toLocalDate().toString() : null
        );
    }

    private ProximoEventoItem toProximoEventoItem(Evento e) {
        return new ProximoEventoItem(
                e.getIdEvento(),
                e.getNombreEvento(),
                e.getFechaEvento() != null ? e.getFechaEvento().toString() : null
        );
    }
}
