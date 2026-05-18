package com.devops.backend.evento.service;

import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.enums.Estado;
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

@Service
@RequiredArgsConstructor
public class EventoAutorizacionServiceImpl implements EventoAutorizacionService {

    private final EventoRepository eventoRepository;
    private final EventoStaffRepository eventoStaffRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public void validarAccesoGestion(Long eventoId) {
        if (tieneRolAdmin()) {
            return;
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        Long idAutenticado = obtenerIdUsuarioAutenticado();

        if (evento.getUsuarioCreador().getIdUsuario().equals(idAutenticado)) {
            return;
        }

        throw new AccessDeniedException("No tienes permisos de gestión sobre este evento. Solo el creador o un admin pueden realizar esta acción.");
    }

    @Override
    @Transactional(readOnly = true)
    public void validarAccesoOperativo(Long eventoId) {
        if (tieneRolAdmin()) {
            return;
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        Long idAutenticado = obtenerIdUsuarioAutenticado();

        // Si es el creador, tiene acceso
        if (evento.getUsuarioCreador().getIdUsuario().equals(idAutenticado)) {
            return;
        }

        // Si no es creador, verificamos si es staff activo
        if (esStaffActivo(eventoId)) {
            return;
        }

        throw new AccessDeniedException("No tienes permisos operativos sobre este evento. Debes ser el organizador o estar asignado como staff activo.");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esStaffActivo(Long eventoId) {
        Long idAutenticado = obtenerIdUsuarioAutenticado();
        return eventoStaffRepository.existsByEvento_IdEventoAndUsuario_IdUsuarioAndEstado(eventoId, idAutenticado, Estado.ACTIVO);
    }

    // --- Métodos de ayuda ---

    private boolean tieneRolAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Long obtenerIdUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return Long.parseLong(auth.getName());
    }
}
