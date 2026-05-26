package com.devops.backend.funcionalidad.service;

import com.devops.backend.funcionalidad.dto.FuncionalidadFilterRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.funcionalidad.dto.FuncionalidadUpdateRequest;

import java.util.List;

public interface FuncionalidadService {

    FuncionalidadResponse save(FuncionalidadRequest request);
    List<FuncionalidadResponse> findAll(FuncionalidadFilterRequest request);

    FuncionalidadResponse findById(Long id);

     FuncionalidadResponse update(Long id, FuncionalidadUpdateRequest request);

    FuncionalidadResponse desactive(Long id);
    public FuncionalidadResponse activar(Long id);
}
