package com.devops.backend.funcionalidad.service;

import com.devops.backend.funcionalidad.dto.FuncionalidadFilterRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.funcionalidad.dto.FuncionalidadUpdateRequest;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface FuncionalidadService {

    FuncionalidadResponse save(FuncionalidadRequest request);
    List<FuncionalidadResponse> findAll(FuncionalidadFilterRequest request);

    FuncionalidadResponse findById(Long id);

     FuncionalidadResponse update(Long id, @Valid FuncionalidadUpdateRequest request);
}
