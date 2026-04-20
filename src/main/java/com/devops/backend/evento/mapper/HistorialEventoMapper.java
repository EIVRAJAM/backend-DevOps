package com.devops.backend.evento.mapper;

import com.devops.backend.evento.dto.HistorialEventoDTO;
import com.devops.backend.evento.entity.HistorialEvento;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades HistorialEvento y DTOs
 */
@Component
public class HistorialEventoMapper {

    /**
     * Convierte una entidad HistorialEvento a HistorialEventoDTO
     *
     * @param historial entidad a convertir
     * @return DTO de respuesta
     */
    public HistorialEventoDTO toDTO(HistorialEvento historial) {
        if (historial == null) {
            return null;
        }

        Long idUsuarioResponsable = null;
        String nombreUsuarioResponsable = null;

        if (historial.getUsuarioResponsable() != null) {
            idUsuarioResponsable = historial.getUsuarioResponsable().getIdUsuario();
            // Concatenar nombres y apellidos del usuario
            Usuario usuario = historial.getUsuarioResponsable();
            nombreUsuarioResponsable = (usuario.getNombres() != null ? usuario.getNombres() : "") + " " +
                    (usuario.getApellidos() != null ? usuario.getApellidos() : "");
            nombreUsuarioResponsable = nombreUsuarioResponsable.trim();
        }

        return new HistorialEventoDTO(
                historial.getIdHistorialEvento(),
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getComentario(),
                idUsuarioResponsable,
                nombreUsuarioResponsable,
                historial.getFechaCambio());
    }

    /**
     * Convierte una lista de HistorialEvento a lista de DTOs
     *
     * @param historiales lista de entidades
     * @return lista de DTOs
     */
    public List<HistorialEventoDTO> toDTOList(List<HistorialEvento> historiales) {
        if (historiales == null) {
            return null;
        }
        return historiales.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
