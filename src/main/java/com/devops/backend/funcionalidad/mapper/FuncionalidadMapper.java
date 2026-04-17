package com.devops.backend.funcionalidad.mapper;

import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.funcionalidad.entity.Funcionalidad;
import org.springframework.stereotype.Component;

@Component
public class FuncionalidadMapper {

    public Funcionalidad toEntity(FuncionalidadRequest request, Funcionalidad padre) {
        Funcionalidad f = new Funcionalidad();
        f.setNombreFuncionalidad(request.nombreFuncionalidad());
        f.setUrlFuncionalidad(request.urlFuncionalidad());
        f.setEstado("ACTIVO");
        f.setPadre(padre); // null si es raíz
        return f;
    }

    public FuncionalidadResponse toResponse(Funcionalidad f) {
        return new FuncionalidadResponse(
                f.getIdFuncionalidad(),
                f.getNombreFuncionalidad(),
                f.getUrlFuncionalidad(),
                f.getEstado(),
                f.getPadre() != null ? f.getPadre().getIdFuncionalidad() : null
        );
    }
}
