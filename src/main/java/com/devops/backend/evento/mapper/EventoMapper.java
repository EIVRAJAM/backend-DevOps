package com.devops.backend.evento.mapper;

import com.devops.backend.evento.dto.CreateEventoDTO;
import com.devops.backend.evento.dto.EventoResponseDTO;
import com.devops.backend.evento.dto.UpdateEventoDTO;
import com.devops.backend.evento.entity.Evento;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Evento y DTOs
 */
@Component
public class EventoMapper {

    /**
     * Convierte una entidad Evento a EventoResponseDTO
     *
     * @param evento entidad a convertir
     * @return DTO de respuesta
     */
    public EventoResponseDTO toDTO(Evento evento) {
        if (evento == null) {
            return null;
        }

        return new EventoResponseDTO(
                evento.getIdEvento(),
                evento.getUsuarioCreador().getIdUsuario(),
                evento.getNombreEvento(),
                evento.getDescripcionEvento(),
                evento.getFechaEvento(),
                evento.getHoraEvento(),
                evento.getLugarEvento(),
                evento.getReferenciaUbicacion(),
                evento.getImagenUrl(),
                evento.getEstadoEvento(),
                evento.getCapacidadMaxima(),
                evento.getTieneParqueadero(),
                evento.getCuposParqueadero(),
                evento.getEsDePago(),
                evento.getPrecio(),
                evento.getMoneda(),
                evento.getCapacidadDisponible(),
                evento.getEstado(),
                evento.getCreadoEn(),
                evento.getActualizadoEn());
    }

    /**
     * Convierte un CreateEventoDTO a una entidad Evento (sin guardar)
     *
     * @param dto            DTO de creación
     * @param usuarioCreador usuario que crea el evento
     * @return entidad Evento (sin persistir)
     */
    public Evento toEntity(CreateEventoDTO dto, com.devops.backend.usuario.entity.Usuario usuarioCreador) {
        if (dto == null) {
            return null;
        }

        Evento evento = new Evento();
        evento.setUsuarioCreador(usuarioCreador);
        evento.setNombreEvento(dto.nombreEvento());
        evento.setDescripcionEvento(dto.descripcionEvento());
        evento.setFechaEvento(dto.fechaEvento());
        evento.setHoraEvento(dto.horaEvento());
        evento.setLugarEvento(dto.lugarEvento());
        evento.setReferenciaUbicacion(dto.referenciaUbicacion());
        evento.setImagenUrl(dto.imagenUrl());
        evento.setCapacidadMaxima(dto.capacidadMaxima());
        evento.setTieneParqueadero(dto.tieneParqueadero());
        evento.setCuposParqueadero(dto.cuposParqueadero());
        evento.setEsDePago(dto.esDePago());

        if (Boolean.TRUE.equals(dto.esDePago())) {
            evento.setPrecio(dto.precio());
            evento.setMoneda(dto.moneda());
        } else {
            evento.setPrecio(null);
            evento.setMoneda(null);
        }

        // Estados por defecto (se asignan en @PrePersist)
        evento.setEstadoEvento(EstadoEvento.BORRADOR);
        evento.setEstado(Estado.ACTIVO);

        return evento;
    }

    /**
     * Actualiza una entidad Evento existente con datos de UpdateEventoDTO
     *
     * @param dto    DTO de actualización (campos opcionales)
     * @param evento entidad a actualizar
     * @return entidad actualizada
     */
    public Evento updateEntity(UpdateEventoDTO dto, Evento evento) {
        if (dto == null || evento == null) {
            return evento;
        }

        if (dto.nombreEvento() != null && !dto.nombreEvento().isBlank()) {
            evento.setNombreEvento(dto.nombreEvento());
        }

        if (dto.descripcionEvento() != null && !dto.descripcionEvento().isBlank()) {
            evento.setDescripcionEvento(dto.descripcionEvento());
        }

        if (dto.fechaEvento() != null) {
            evento.setFechaEvento(dto.fechaEvento());
        }

        if (dto.horaEvento() != null) {
            evento.setHoraEvento(dto.horaEvento());
        }

        if (dto.lugarEvento() != null && !dto.lugarEvento().isBlank()) {
            evento.setLugarEvento(dto.lugarEvento());
        }

        if (dto.referenciaUbicacion() != null && !dto.referenciaUbicacion().isBlank()) {
            evento.setReferenciaUbicacion(dto.referenciaUbicacion());
        }

        if (dto.imagenUrl() != null && !dto.imagenUrl().isBlank()) {
            evento.setImagenUrl(dto.imagenUrl());
        }

        if (dto.capacidadMaxima() != null) {
            evento.setCapacidadMaxima(dto.capacidadMaxima());
        }

        if (dto.tieneParqueadero() != null) {
            evento.setTieneParqueadero(dto.tieneParqueadero());
            if (Boolean.FALSE.equals(dto.tieneParqueadero())) {
                evento.setCuposParqueadero(0);
            } else if (dto.cuposParqueadero() != null) {
                evento.setCuposParqueadero(dto.cuposParqueadero());
            }
        }

        if (dto.esDePago() != null) {
            evento.setEsDePago(dto.esDePago());
            if (Boolean.TRUE.equals(dto.esDePago())) {
                if (dto.precio() != null) evento.setPrecio(dto.precio());
                if (dto.moneda() != null) evento.setMoneda(dto.moneda());
            } else {
                evento.setPrecio(null);
                evento.setMoneda(null);
            }
        } else {
            if (dto.precio() != null) {
                evento.setPrecio(dto.precio());
            }
            if (dto.moneda() != null) {
                evento.setMoneda(dto.moneda());
            }
        }

        return evento;
    }

    /**
     * Convierte una lista de eventos a lista de DTOs
     *
     * @param eventos lista de entidades
     * @return lista de DTOs
     */
    public List<EventoResponseDTO> toDTOList(List<Evento> eventos) {
        if (eventos == null) {
            return null;
        }
        return eventos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
