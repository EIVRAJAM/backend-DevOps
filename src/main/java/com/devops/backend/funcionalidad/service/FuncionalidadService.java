package com.devops.backend.funcionalidad.service;

import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;

import java.util.List;

public interface FuncionalidadService {

    FuncionalidadResponse save(FuncionalidadRequest request);
    List<FuncionalidadResponse> findAll(String status, Long id_padre);

}
