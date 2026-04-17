package com.devops.backend.sesion.service;

import com.devops.backend.sesion.dto.SesionFilterRequest;
import com.devops.backend.sesion.dto.SesionResponseDto;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface SesionService {

    Page<SesionResponseDto> getAllSesiones(SesionFilterRequest filter);

    Page<SesionResponseDto> getSesionesActivas(int page, int size);

    Optional<SesionResponseDto> getUltimaSesionByUsuario(Long idUsuario);
}
