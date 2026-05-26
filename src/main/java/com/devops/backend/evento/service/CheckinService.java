package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.CheckinRequestDTO;
import com.devops.backend.evento.dto.CheckinResponseDTO;
import com.devops.backend.evento.dto.CheckinResumenDTO;
import com.devops.backend.evento.dto.EstadoCheckinDTO;

public interface CheckinService {

    CheckinResponseDTO realizarCheckin(Long eventoId, CheckinRequestDTO request);

    CheckinResumenDTO obtenerResumen(Long eventoId);

    EstadoCheckinDTO obtenerEstadoCheckin(Long eventoId);
}
