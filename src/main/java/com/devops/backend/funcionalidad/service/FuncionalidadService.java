package com.devops.backend.funcionalidad.service;

import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;

public interface FuncionalidadService {

    FuncionalidadResponse save(FuncionalidadRequest request);

}
