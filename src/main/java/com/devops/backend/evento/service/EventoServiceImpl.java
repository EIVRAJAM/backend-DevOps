package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.ComentarioRequest;
import com.devops.backend.evento.dto.CreateEventoDTO;
import com.devops.backend.evento.dto.EventoResponseDTO;
import com.devops.backend.evento.dto.HistorialEventoDTO;
import com.devops.backend.evento.dto.UpdateEventoDTO;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.HistorialEvento;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import com.devops.backend.evento.exception.EventoNoEditableException;
import com.devops.backend.evento.mapper.EventoMapper;
import com.devops.backend.evento.mapper.HistorialEventoMapper;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.evento.repository.HistorialEventoRepository;
import com.devops.backend.evento.specification.EventoSpecification;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

        private final EventoRepository eventoRepository;
        private final UsuarioRepository usuarioRepository;
        private final HistorialEventoRepository historialEventoRepository;
        private final EventoMapper eventoMapper;
        private final HistorialEventoMapper historialEventoMapper;
        private final EventoSpecification eventoSpecification;

        @Override
        public EventoResponseDTO crearEvento(CreateEventoDTO createEventoDTO) {
                Usuario usuarioCreador = obtenerUsuarioAutenticado();

                Evento evento = eventoMapper.toEntity(createEventoDTO, usuarioCreador);
                Evento eventoGuardado = eventoRepository.save(evento);

                return eventoMapper.toDTO(eventoGuardado);
        }

        @Override
        @Transactional(readOnly = true)
        public EventoResponseDTO obtenerEventoPorId(Long idEvento) {
                Evento evento = obtenerEvento(idEvento);
                return eventoMapper.toDTO(evento);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<EventoResponseDTO> listarEventos(
                        Pageable pageable,
                        EstadoEvento estadoEvento,
                        Estado estado,
                        String nombreEvento,
                        String lugarEvento,
                        Long idUsuarioCreador,
                        LocalDate fechaInicio,
                        LocalDate fechaFin) {

                Specification<Evento> spec = null;

                if (estadoEvento != null) {
                        spec = Specification.where(
                                        eventoSpecification.filtrarPorEstadoEvento(estadoEvento));
                }

                if (estado != null) {
                        spec = (spec == null)
                                        ? Specification.where(eventoSpecification.filtrarPorEstado(estado))
                                        : spec.and(eventoSpecification.filtrarPorEstado(estado));
                }

                if (nombreEvento != null && !nombreEvento.isBlank()) {
                        spec = (spec == null)
                                        ? Specification.where(eventoSpecification.filtrarPorNombre(nombreEvento))
                                        : spec.and(eventoSpecification.filtrarPorNombre(nombreEvento));
                }

                if (lugarEvento != null && !lugarEvento.isBlank()) {
                        spec = (spec == null)
                                        ? Specification.where(eventoSpecification.filtrarPorLugar(lugarEvento))
                                        : spec.and(eventoSpecification.filtrarPorLugar(lugarEvento));
                }

                if (idUsuarioCreador != null) {
                        spec = (spec == null)
                                        ? Specification.where(
                                                        eventoSpecification.filtrarPorUsuarioCreador(idUsuarioCreador))
                                        : spec.and(eventoSpecification.filtrarPorUsuarioCreador(idUsuarioCreador));
                }

                if (fechaInicio != null && fechaFin != null) {
                        spec = (spec == null)
                                        ? Specification.where(
                                                        eventoSpecification.filtrarPorFecha(fechaInicio, fechaFin))
                                        : spec.and(eventoSpecification.filtrarPorFecha(fechaInicio, fechaFin));
                }

                return eventoRepository.findAll(spec, pageable)
                                .map(eventoMapper::toDTO);
        }

        @Override
        public EventoResponseDTO actualizarEvento(
                        Long idEvento,
                        UpdateEventoDTO updateEventoDTO) {

                Evento evento = obtenerEvento(idEvento);

                validarAutorizacion(evento);

                if (evento.getEstadoEvento() == EstadoEvento.CERRADO
                                || evento.getEstadoEvento() == EstadoEvento.CANCELADO) {
                        throw new EventoNoEditableException(
                                        "No se puede editar un evento en estado " + evento.getEstadoEvento());
                }

                validarParqueadero(updateEventoDTO);

                Evento eventoActualizado = eventoMapper.updateEntity(updateEventoDTO, evento);

                return eventoMapper.toDTO(
                                eventoRepository.save(eventoActualizado));
        }

        @Override
        @Transactional(readOnly = true)
        public Page<EventoResponseDTO> listarEventosPorUsuario(
                        Long idUsuario,
                        Pageable pageable) {

                usuarioRepository.findById(idUsuario)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario no encontrado"));

                return eventoRepository
                                .findByUsuarioCreador_IdUsuarioAndEstado(
                                                idUsuario,
                                                Estado.ACTIVO,
                                                pageable)
                                .map(eventoMapper::toDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<HistorialEventoDTO> obtenerHistorialEvento(Long idEvento) {
                obtenerEvento(idEvento);

                return historialEventoMapper.toDTOList(
                                historialEventoRepository
                                                .findByEvento_IdEventoOrderByFechaCambioDesc(idEvento));
        }

        @Override
        public EventoResponseDTO publicarEvento(Long idEvento) {
                Evento evento = obtenerEvento(idEvento);

                validarAutorizacion(evento);

                if (evento.getEstadoEvento() != EstadoEvento.BORRADOR) {
                        throw new ConflictException(
                                        "Solo se pueden publicar eventos en BORRADOR");
                }

                registrarHistorial(
                                evento,
                                evento.getEstadoEvento(),
                                EstadoEvento.PUBLICADO,
                                "Evento publicado");

                evento.setEstadoEvento(EstadoEvento.PUBLICADO);

                return eventoMapper.toDTO(
                                eventoRepository.save(evento));
        }

        @Override
        public EventoResponseDTO cancelarEvento(
                        Long idEvento,
                        ComentarioRequest comentarioRequest) {

                Evento evento = obtenerEvento(idEvento);

                validarAutorizacion(evento);

                EstadoEvento actual = evento.getEstadoEvento();

                if (actual != EstadoEvento.BORRADOR
                                && actual != EstadoEvento.PUBLICADO) {
                        throw new ConflictException(
                                        "Solo se puede cancelar en BORRADOR o PUBLICADO");
                }

                registrarHistorial(
                                evento,
                                actual,
                                EstadoEvento.CANCELADO,
                                comentarioRequest.comentario());

                evento.setEstadoEvento(EstadoEvento.CANCELADO);

                return eventoMapper.toDTO(
                                eventoRepository.save(evento));
        }

        @Override
        public EventoResponseDTO cerrarEvento(Long idEvento) {
                Evento evento = obtenerEvento(idEvento);

                validarAutorizacion(evento);

                if (evento.getEstadoEvento() != EstadoEvento.PUBLICADO) {
                        throw new ConflictException(
                                        "Solo se puede cerrar un evento PUBLICADO");
                }

                registrarHistorial(
                                evento,
                                evento.getEstadoEvento(),
                                EstadoEvento.CERRADO,
                                "Evento cerrado");

                evento.setEstadoEvento(EstadoEvento.CERRADO);

                return eventoMapper.toDTO(
                                eventoRepository.save(evento));
        }

        @Override
        public EventoResponseDTO activarEvento(Long idEvento) {
                Evento evento = obtenerEvento(idEvento);

                validarAutorizacion(evento);

                if (evento.getEstado() != Estado.INACTIVO) {
                        throw new ConflictException(
                                        "Solo se puede activar desde INACTIVO");
                }

                evento.setEstado(Estado.ACTIVO);

                return eventoMapper.toDTO(
                                eventoRepository.save(evento));
        }

        @Override
        public EventoResponseDTO desactivarEvento(
                        Long idEvento,
                        ComentarioRequest comentarioRequest) {

                Evento evento = obtenerEvento(idEvento);

                validarAutorizacion(evento);

                if (evento.getEstado() != Estado.ACTIVO) {
                        throw new ConflictException(
                                        "Solo se puede desactivar desde ACTIVO");
                }

                evento.setEstado(Estado.INACTIVO);

                return eventoMapper.toDTO(
                                eventoRepository.save(evento));
        }

        private Evento obtenerEvento(Long idEvento) {
                return eventoRepository.findById(idEvento)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Evento con ID " + idEvento + " no encontrado"));
        }

        private Usuario obtenerUsuarioAutenticado() {
                Long idUsuario = obtenerIdUsuarioAutenticado();

                return usuarioRepository.findById(idUsuario)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario autenticado no encontrado"));
        }

        private Long obtenerIdUsuarioAutenticado() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth == null || !auth.isAuthenticated()) {
                        throw new RuntimeException("No hay usuario autenticado");
                }

                return Long.parseLong(auth.getName());
        }

        private void validarAutorizacion(Evento evento) {
                Long idUsuario = obtenerIdUsuarioAutenticado();
                Long idCreador = evento.getUsuarioCreador().getIdUsuario();

                if (!idCreador.equals(idUsuario) && !tieneRolAdmin()) {
                        throw new EventoNoEditableException(
                                        "No tienes permisos para esta acción");
                }
        }

        private boolean tieneRolAdmin() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                return auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        private void registrarHistorial(
                        Evento evento,
                        EstadoEvento anterior,
                        EstadoEvento nuevo,
                        String comentario) {

                HistorialEvento historial = new HistorialEvento();
                historial.setEvento(evento);
                historial.setEstadoAnterior(anterior);
                historial.setEstadoNuevo(nuevo);
                historial.setComentario(comentario);
                historial.setUsuarioResponsable(obtenerUsuarioAutenticado());

                historialEventoRepository.save(historial);
        }

        private void validarParqueadero(UpdateEventoDTO dto) {
                if (dto.tieneParqueadero() == null) {
                        return;
                }

                boolean tiene = dto.tieneParqueadero();
                Integer cupos = dto.cuposParqueadero();

                if (tiene && (cupos == null || cupos < 0)) {
                        throw new ConflictException(
                                        "Si tiene parqueadero, cupos debe ser >= 0");
                }

                if (!tiene && cupos != null && cupos > 0) {
                        throw new ConflictException(
                                        "Si no tiene parqueadero, cupos debe ser 0");
                }
        }
}