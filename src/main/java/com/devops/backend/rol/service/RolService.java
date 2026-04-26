package com.devops.backend.rol.service;

import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.rol.entity.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface RolService {

    RolResponse save(RolRequest request);
    List<RolResponse> findAll();
    Page<RolResponse> findAllFilter(RolFilterRequest filter);
    RolResponse findById(Long id);
    RolResponse findByName(String name);
    RolResponse desactivarRol(Long id);
    RolResponse activarRol(Long id);
    RolResponse updateRol(Long id, RolUpdateDto request);
    RolResponse asignarFuncionalidades(Long idRol, AsignarFuncionalidadesRequest request);
    List<FuncionalidadResponse> findFuncionalidadesByRol(Long idRol);
    RolResponse eliminarFuncionalidad(Long idRol, Long idFuncionalidad);
}
