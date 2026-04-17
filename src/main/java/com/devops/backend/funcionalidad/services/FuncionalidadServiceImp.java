package com.devops.backend.funcionalidad.services;

import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.funcionalidad.entity.Funcionalidad;
import com.devops.backend.funcionalidad.mapper.FuncionalidadMapper;
import com.devops.backend.funcionalidad.repository.FuncionalidadRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FuncionalidadServiceImp  implements FuncionalidadService{


    private final FuncionalidadRepository funcionalidadRepository;
    private final FuncionalidadMapper funcionalidadMapper;

    public FuncionalidadServiceImp(FuncionalidadRepository funcionalidadRepository,
                                   FuncionalidadMapper funcionalidadMapper) {
        this.funcionalidadRepository = funcionalidadRepository;
        this.funcionalidadMapper = funcionalidadMapper;
    }

    @Override
    public FuncionalidadResponse save(FuncionalidadRequest request) {

        List<ApiValidationError> errors = new ArrayList<>();

        if (funcionalidadRepository.existsByNombreFuncionalidad(request.nombreFuncionalidad())) {
            errors.add(new ApiValidationError("nombreFuncionalidad", "Ya existe una funcionalidad con ese nombre"));
        }

        // 2. Resolver el padre si viene idPadre
        Funcionalidad padre = null;
        if (request.idPadre() != null) {
            padre = funcionalidadRepository.findById(request.idPadre())
                    .orElseGet(() -> {
                        errors.add(new ApiValidationError("idPadre", "La funcionalidad padre con ID " + request.idPadre() + " no existe"));
                        return null;
                    });
        }

        if (!errors.isEmpty()) {
            throw new ConflictException("Campos inválidos en el registro", errors);
        }

        // 3. Persistir
        Funcionalidad saved = funcionalidadRepository.save(
                funcionalidadMapper.toEntity(request, padre)
        );

        return funcionalidadMapper.toResponse(saved);
    }

    private void validaciones(FuncionalidadRequest request){

    }
}
