package com.devops.backend.rol.mapper;

import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.funcionalidad.mapper.FuncionalidadMapper;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.entity.dto.RolResponse;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class RolMapper {

    private FuncionalidadMapper funcionalidadMapper;

    public Rol toEntity(RolRequest request) {
        Rol rol = new Rol();
        rol.setNombreRol(request.nombreRol());
        rol.setEstado("ACTIVO");
        return rol;
    }

    public RolResponse toResponse(Rol rol) {
        Set<FuncionalidadResponse> funcionalidades = rol.getFuncionalidades() == null
                ? Collections.emptySet()
                : rol.getFuncionalidades().stream()
                .map(funcionalidadMapper::toResponse)
                .collect(Collectors.toSet());

        return new RolResponse(
                rol.getIdRol(),
                rol.getNombreRol(),
                rol.getEstado(),
                rol.getCreadoEn(),
                rol.getActualizadoEn(),
                funcionalidades
        );
    }

}
