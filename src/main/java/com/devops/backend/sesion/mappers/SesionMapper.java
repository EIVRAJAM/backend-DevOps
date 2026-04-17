package com.devops.backend.sesion.mappers;

import com.devops.backend.sesion.dto.SesionResponseDto;
import com.devops.backend.sesion.entity.Sesion;
import org.springframework.stereotype.Component;

@Component
public class SesionMapper {

    public SesionResponseDto toResponse(Sesion sesion) {
        return new SesionResponseDto(
                sesion.getIdSesion(),
                sesion.getUsuario().getIdUsuario(),
                sesion.getUsuario().getNombres(),
                sesion.getUsuario().getApellidos(),
                sesion.getFechaSesion(),
                sesion.getHoraSesion());
    }
}
