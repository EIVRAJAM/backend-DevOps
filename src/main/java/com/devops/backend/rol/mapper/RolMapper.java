package com.devops.backend.rol.mapper;

import com.devops.backend.rol.enums.Estado;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.funcionalidad.mapper.FuncionalidadMapper;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.dto.RolRequest;
import com.devops.backend.rol.dto.RolResponse;
import com.devops.backend.rol.dto.RolUpdateDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RolMapper {



    private FuncionalidadMapper funcionalidadMapper;

    public RolMapper(FuncionalidadMapper funcionalidadMapper) {
        this.funcionalidadMapper = funcionalidadMapper;
    }

    public Rol toEntity(RolRequest request) {
        Rol rol = new Rol();
        rol.setNombreRol(request.nombreRol());
        rol.setEstado(Estado.ACTIVO);
        return rol;
    }

    public RolResponse toResponse(Rol rol) {
        List<FuncionalidadResponse> funcionalidades = getFuncionalidadByRol(rol);

        return new RolResponse(
                rol.getIdRol(),
                rol.getNombreRol(),
                rol.getEstado(),
                rol.getCreadoEn(),
                rol.getActualizadoEn(),
                funcionalidades
        );
    }

    public List<FuncionalidadResponse> getFuncionalidadByRol(Rol rol){
        return rol.getFuncionalidades() == null
                ? List.of()
                : rol.getFuncionalidades().stream()
                .map(funcionalidadMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void applyUpdate(Rol rol, RolUpdateDto r){
        rol.setNombreRol(r.nombreRol());
        rol.setEstado(r.estado());
    }
}
