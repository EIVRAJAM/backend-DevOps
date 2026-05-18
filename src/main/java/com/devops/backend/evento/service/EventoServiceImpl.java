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
import com.devops.backend.evento.repository.TicketRepository;
import com.devops.backend.evento.specification.EventoSpecification;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
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
        private final TicketRepository ticketRepository;
        private final EventoMapper eventoMapper;
        private final HistorialEventoMapper historialEventoMapper;
        private final EventoSpecification eventoSpecification;
        private final EventoAutorizacionService autorizacionService;

        @Override
        public EventoResponseDTO crearEvento(CreateEventoDTO createEventoDTO) {
                // Solo ORGANIZER y ADMIN pueden crear eventos
                if (tieneRolUser()) {
                        throw new AccessDeniedException(
                                        "Los usuarios no pueden crear eventos. Se requiere rol ORGANIZER o ADMIN.");
                }

                Usuario usuarioCreador = obtenerUsuarioAutenticado();

                Evento evento = eventoMapper.toEntity(createEventoDTO, usuarioCreador);
                Evento eventoGuardado = eventoRepository.save(evento);

                return eventoMapper.toDTO(eventoGuardado);
        }

        @Override
        @Transactional(readOnly = true)
        public EventoResponseDTO obtenerEventoPorId(Long idEvento) {
                Evento evento = obtenerEvento(idEvento);

                // ADMIN: ve cualquier evento sin restricción
                if (tieneRolAdmin()) {
                        return eventoMapper.toDTO(evento);
                }

                // ORGANIZER: solo puede ver detalle de sus propios eventos
                if (tieneRolOrganizer()) {
                        Long idAutenticado = obtenerIdUsuarioAutenticado();
                        if (!evento.getUsuarioCreador().getIdUsuario().equals(idAutenticado)) {
                                throw new AccessDeniedException(
                                                "Solo puedes ver el detalle de tus propios eventos.");
                        }
                        return eventoMapper.toDTO(evento);
                }

                // STAFF: Si el usuario está asignado como staff activo, puede ver el evento
                try {
                        if (autorizacionService.esStaffActivo(idEvento)) {
                                return eventoMapper.toDTO(evento);
                        }
                } catch (Exception e) {
                        // Ignorar y caer en el denegado
                }

                // ROLE_USER: no tiene acceso a este endpoint de gestión.
                // Debe usar GET /api/v1/eventos/disponibles/{id}
                throw new AccessDeniedException(
                                "Acceso denegado. Usa GET /api/v1/eventos/disponibles para explorar eventos.");
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

                // ── Control de acceso por rol ────────────────────────────────────────────
                // Este endpoint es exclusivo del ADMIN.
                // ORGANIZER y USER deben usar sus endpoints dedicados.
                if (!tieneRolAdmin()) {
                        if (tieneRolOrganizer()) {
                                throw new AccessDeniedException(
                                                "Los organizadores deben usar GET /api/v1/eventos/mis-eventos.");
                        }
                        throw new AccessDeniedException(
                                        "Acceso denegado. Usa GET /api/v1/eventos/disponibles para ver eventos.");
                }
                // ────────────────────────────────────────────────────────────────────────

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

                return eventoRepository.findAll(spec, pageable).map(eventoMapper::toDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<EventoResponseDTO> listarEventosDisponibles(
                        Pageable pageable,
                        String nombre,
                        String lugar,
                        LocalDate fechaInicio,
                        LocalDate fechaFin,
                        Boolean esDePago,
                        Boolean conCupos) {

                // Base: PUBLICADO + ACTIVO, ordenado siempre por fecha ASC
                Pageable sortedPageable = PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                Sort.by(Sort.Direction.ASC, "fechaEvento"));

                Specification<Evento> spec = Specification
                                .where(eventoSpecification.filtrarPorEstadoEvento(EstadoEvento.PUBLICADO))
                                .and(eventoSpecification.filtrarPorEstado(Estado.ACTIVO));

                if (nombre != null && !nombre.isBlank()) {
                        spec = spec.and(eventoSpecification.filtrarPorNombre(nombre));
                }
                if (lugar != null && !lugar.isBlank()) {
                        spec = spec.and(eventoSpecification.filtrarPorLugar(lugar));
                }
                if (fechaInicio != null && fechaFin != null) {
                        spec = spec.and(eventoSpecification.filtrarPorFecha(fechaInicio, fechaFin));
                }
                if (esDePago != null) {
                        spec = spec.and(eventoSpecification.filtrarPorEsDePago(esDePago));
                }
                if (Boolean.TRUE.equals(conCupos)) {
                        spec = spec.and(eventoSpecification.filtrarConCuposDisponibles());
                }

                return eventoRepository.findAll(spec, sortedPageable).map(eventoMapper::toDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public EventoResponseDTO obtenerEventoDisponiblePorId(Long idEvento) {
                Evento evento = obtenerEvento(idEvento);
                if (evento.getEstadoEvento() != EstadoEvento.PUBLICADO || evento.getEstado() != Estado.ACTIVO) {
                        throw new AccessDeniedException("El evento no está disponible.");
                }
                return eventoMapper.toDTO(evento);
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
                validarCapacidadMaxima(updateEventoDTO, evento);

                Evento eventoActualizado = eventoMapper.updateEntity(updateEventoDTO, evento);

                return eventoMapper.toDTO(
                                eventoRepository.save(eventoActualizado));
        }

        @Override
        @Transactional(readOnly = true)
        public Page<EventoResponseDTO> listarEventosPorUsuario(
                        Long idUsuario,
                        Pageable pageable) {

                // Solo ADMIN puede listar eventos de cualquier usuario por ID
                if (!tieneRolAdmin()) {
                        throw new AccessDeniedException(
                                        "Solo los administradores pueden listar eventos de otro usuario. "
                                                        + "Usa GET /api/v1/eventos/mis-eventos para ver tus propios eventos.");
                }

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
        public Page<EventoResponseDTO> listarMisEventos(
                        Pageable pageable,
                        EstadoEvento estadoEvento,
                        Estado estado,
                        String nombreEvento,
                        String lugarEvento,
                        LocalDate fechaInicio,
                        LocalDate fechaFin) {

                Long idAutenticado = obtenerIdUsuarioAutenticado();

                // Solo ORGANIZER y ADMIN pueden ver sus propios eventos
                if (tieneRolUser()) {
                        throw new AccessDeniedException(
                                        "Acceso denegado: usa GET /api/v1/eventos/disponibles para ver eventos.");
                }

                // Siempre filtrado por el usuario autenticado
                Specification<Evento> spec = Specification.where(
                                eventoSpecification.filtrarPorUsuarioCreador(idAutenticado));

                if (estadoEvento != null) {
                        spec = spec.and(eventoSpecification.filtrarPorEstadoEvento(estadoEvento));
                }
                if (estado != null) {
                        spec = spec.and(eventoSpecification.filtrarPorEstado(estado));
                }
                if (nombreEvento != null && !nombreEvento.isBlank()) {
                        spec = spec.and(eventoSpecification.filtrarPorNombre(nombreEvento));
                }
                if (lugarEvento != null && !lugarEvento.isBlank()) {
                        spec = spec.and(eventoSpecification.filtrarPorLugar(lugarEvento));
                }
                if (fechaInicio != null && fechaFin != null) {
                        spec = spec.and(eventoSpecification.filtrarPorFecha(fechaInicio, fechaFin));
                }

                return eventoRepository.findAll(spec, pageable).map(eventoMapper::toDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<HistorialEventoDTO> obtenerHistorialEvento(Long idEvento) {
                Evento evento = obtenerEvento(idEvento);

                // USER no puede ver historial (es un endpoint de gestión)
                if (tieneRolUser()) {
                        throw new AccessDeniedException(
                                        "Acceso denegado: el historial de eventos es un recurso de gestión.");
                }

                // ORGANIZER solo puede ver el historial de sus propios eventos
                if (!tieneRolAdmin()) {
                        Long idAutenticado = obtenerIdUsuarioAutenticado();
                        if (!evento.getUsuarioCreador().getIdUsuario().equals(idAutenticado)) {
                                throw new AccessDeniedException(
                                                "Solo puedes ver el historial de tus propios eventos.");
                        }
                }

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

        private boolean tieneRolOrganizer() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                return auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"));
        }

        private boolean tieneRolUser() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                return auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
                                && !tieneRolAdmin() && !tieneRolOrganizer();
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

        /**
         * Impide reducir capacidadMaxima por debajo de los cupos ya ocupados.
         * Cupos ocupados = capacidadMaxima - capacidadDisponible.
         */
        private void validarCapacidadMaxima(UpdateEventoDTO dto, Evento evento) {
                if (dto.capacidadMaxima() == null) {
                        return;
                }
                int nueva = dto.capacidadMaxima();
                int actual = evento.getCapacidadMaxima() != null ? evento.getCapacidadMaxima() : 0;
                int disponible = evento.getCapacidadDisponible() != null ? evento.getCapacidadDisponible() : 0;
                int ocupados = actual - disponible;

                if (nueva < ocupados) {
                        throw new ConflictException(
                                        "No se puede reducir la capacidad máxima a " + nueva
                                                        + " porque ya hay " + ocupados + " cupos ocupados.");
                }

                // Ajustar capacidadDisponible proporcionalmente
                int nuevaDisponible = nueva - ocupados;
                evento.setCapacidadDisponible(nuevaDisponible);
        }
}