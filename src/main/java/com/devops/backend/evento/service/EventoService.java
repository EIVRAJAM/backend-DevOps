package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.ComentarioRequest;
import com.devops.backend.evento.dto.CreateEventoDTO;
import com.devops.backend.evento.dto.EventoResponseDTO;
import com.devops.backend.evento.dto.HistorialEventoDTO;
import com.devops.backend.evento.dto.UpdateEventoDTO;
import com.devops.backend.evento.enums.Estado;
import com.devops.backend.evento.enums.EstadoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface EventoService {

    EventoResponseDTO crearEvento(CreateEventoDTO createEventoDTO);

    EventoResponseDTO obtenerEventoPorId(Long idEvento);

    Page<EventoResponseDTO> listarEventos(
            Pageable pageable,
            EstadoEvento estadoEvento,
            Estado estado,
            String nombreEvento,
            String lugarEvento,
            Long idUsuarioCreador,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    EventoResponseDTO actualizarEvento(Long idEvento, UpdateEventoDTO updateEventoDTO);

    Page<EventoResponseDTO> listarEventosPorUsuario(Long idUsuario, Pageable pageable);

    List<HistorialEventoDTO> obtenerHistorialEvento(Long idEvento);

    EventoResponseDTO publicarEvento(Long idEvento);

    EventoResponseDTO cancelarEvento(Long idEvento, ComentarioRequest comentarioRequest);

    EventoResponseDTO cerrarEvento(Long idEvento);

    EventoResponseDTO activarEvento(Long idEvento);

    EventoResponseDTO desactivarEvento(Long idEvento, ComentarioRequest comentarioRequest);
}