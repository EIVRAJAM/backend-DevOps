package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.AsignarStaffRequestDTO;
import com.devops.backend.evento.dto.MisAsignacionesStaffDTO;
import com.devops.backend.evento.dto.StaffResponseDTO;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.entity.EventoStaff;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.exception.StaffNoEncontradoException;
import com.devops.backend.evento.exception.StaffYaAsignadoException;
import com.devops.backend.evento.repository.EventoRepository;
import com.devops.backend.evento.repository.EventoStaffRepository;
import com.devops.backend.exception.ResourceNotFoundException;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoStaffServiceImpl implements EventoStaffService {

    private final EventoStaffRepository eventoStaffRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoAutorizacionService autorizacionService;

    @Override
    @Transactional
    public StaffResponseDTO asignarStaff(Long eventoId, AsignarStaffRequestDTO request) {
        autorizacionService.validarAccesoGestion(eventoId);

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        Usuario usuarioAsignar = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario a asignar no encontrado"));

        Usuario asignador = obtenerUsuarioAutenticado();

        Optional<EventoStaff> existente = eventoStaffRepository.findByEvento_IdEventoAndUsuario_IdUsuario(eventoId, request.idUsuario());

        if (existente.isPresent()) {
            EventoStaff staff = existente.get();
            if (staff.getEstado() == Estado.ACTIVO) {
                throw new StaffYaAsignadoException("El usuario ya está asignado como staff activo para este evento");
            } else {
                throw new StaffYaAsignadoException("El usuario ya está asignado pero inactivo. Usa el endpoint para reactivarlo.");
            }
        }

        EventoStaff nuevoStaff = new EventoStaff();
        nuevoStaff.setEvento(evento);
        nuevoStaff.setUsuario(usuarioAsignar);
        nuevoStaff.setAsignadoPor(asignador);
        nuevoStaff.setEstado(Estado.ACTIVO);

        nuevoStaff = eventoStaffRepository.save(nuevoStaff);

        return toStaffResponseDTO(nuevoStaff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponseDTO> listarStaff(Long eventoId) {
        autorizacionService.validarAccesoGestion(eventoId);
        
        // Verificamos que el evento exista
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento no encontrado");
        }

        // Listamos todo el staff, activo e inactivo, para que el organizador pueda gestionar
        return eventoStaffRepository.findAll()
                .stream()
                .filter(es -> es.getEvento().getIdEvento().equals(eventoId))
                .map(this::toStaffResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StaffResponseDTO activarStaff(Long eventoId, Long usuarioId) {
        autorizacionService.validarAccesoGestion(eventoId);

        EventoStaff staff = eventoStaffRepository.findByEvento_IdEventoAndUsuario_IdUsuario(eventoId, usuarioId)
                .orElseThrow(() -> new StaffNoEncontradoException("Asignación de staff no encontrada para el evento y usuario especificados"));

        if (staff.getEstado() == Estado.ACTIVO) {
            throw new StaffYaAsignadoException("El staff ya se encuentra activo");
        }

        staff.setEstado(Estado.ACTIVO);
        staff = eventoStaffRepository.save(staff);

        return toStaffResponseDTO(staff);
    }

    @Override
    @Transactional
    public StaffResponseDTO desactivarStaff(Long eventoId, Long usuarioId) {
        autorizacionService.validarAccesoGestion(eventoId);

        EventoStaff staff = eventoStaffRepository.findByEvento_IdEventoAndUsuario_IdUsuario(eventoId, usuarioId)
                .orElseThrow(() -> new StaffNoEncontradoException("Asignación de staff no encontrada para el evento y usuario especificados"));

        if (staff.getEstado() == Estado.INACTIVO) {
            throw new StaffYaAsignadoException("El staff ya se encuentra inactivo");
        }

        staff.setEstado(Estado.INACTIVO);
        staff = eventoStaffRepository.save(staff);

        return toStaffResponseDTO(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MisAsignacionesStaffDTO> obtenerMisAsignaciones() {
        Long idAutenticado = obtenerIdUsuarioAutenticado();

        List<EventoStaff> asignaciones = eventoStaffRepository.findByUsuario_IdUsuarioAndEstadoWithEvento(idAutenticado, Estado.ACTIVO);

        return asignaciones.stream()
                .map(staff -> {
                    Evento e = staff.getEvento();
                    return new MisAsignacionesStaffDTO(
                            e.getIdEvento(),
                            e.getNombreEvento(),
                            e.getFechaEvento(),
                            e.getHoraEvento(),
                            e.getLugarEvento(),
                            e.getEstadoEvento()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneAsignacionesActivas() {
        Long idAutenticado = obtenerIdUsuarioAutenticado();
        return eventoStaffRepository.existsByUsuario_IdUsuarioAndEstado(idAutenticado, Estado.ACTIVO);
    }

    // --- Helpers ---

    private StaffResponseDTO toStaffResponseDTO(EventoStaff staff) {
        String nombreCompleto = staff.getUsuario().getNombres() + " " + staff.getUsuario().getApellidos();
        return new StaffResponseDTO(
                staff.getIdEventoStaff(),
                staff.getEvento().getIdEvento(),
                staff.getUsuario().getIdUsuario(),
                nombreCompleto,
                staff.getEstado(),
                staff.getCreadoEn()
        );
    }

    private Long obtenerIdUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return Long.parseLong(auth.getName());
    }
    
    private Usuario obtenerUsuarioAutenticado() {
        Long idUsuario = obtenerIdUsuarioAutenticado();
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}
