package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.AsignarStaffRequestDTO;
import com.devops.backend.evento.dto.MisAsignacionesStaffDTO;
import com.devops.backend.evento.dto.StaffResponseDTO;

import java.util.List;

public interface EventoStaffService {

    StaffResponseDTO asignarStaff(Long eventoId, AsignarStaffRequestDTO request);

    List<StaffResponseDTO> listarStaff(Long eventoId);

    StaffResponseDTO activarStaff(Long eventoId, Long usuarioId);

    StaffResponseDTO desactivarStaff(Long eventoId, Long usuarioId);

    List<MisAsignacionesStaffDTO> obtenerMisAsignaciones();

    boolean tieneAsignacionesActivas();
}
