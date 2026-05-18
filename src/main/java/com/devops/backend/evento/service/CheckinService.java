package com.devops.backend.evento.service;

import com.devops.backend.evento.dto.CheckinRequestDTO;
import com.devops.backend.evento.dto.CheckinResponseDTO;
import com.devops.backend.evento.dto.CheckinResumenDTO;

public interface CheckinService {

    CheckinResponseDTO realizarCheckin(Long eventoId, CheckinRequestDTO request);

    CheckinResumenDTO obtenerResumen(Long eventoId);
}
