package com.devops.backend.funcionalidad.service;

import com.devops.backend.funcionalidad.dto.FuncionalidadFilterRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.funcionalidad.dto.FuncionalidadUpdateRequest;
import com.devops.backend.funcionalidad.entity.Funcionalidad;
import com.devops.backend.funcionalidad.mapper.FuncionalidadMapper;
import com.devops.backend.funcionalidad.repository.FuncionalidadRepository;
import com.devops.backend.funcionalidad.specification.FuncionalidadSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        Funcionalidad padre = null;
        validaciones(request);

        if (request.idPadre() != null) {
            padre = funcionalidadRepository.findById(request.idPadre())
                    .orElseGet(() -> {
                        new ApiValidationError("idPadre", "La funcionalidad padre con ID " + request.idPadre() + " no existe");
                        return null;
                    });
        }


        Funcionalidad saved = funcionalidadRepository.save(
                funcionalidadMapper.toEntity(request, padre)
        );

        return funcionalidadMapper.toResponse(saved);
    }

    @Override
    public List<FuncionalidadResponse> findAll(FuncionalidadFilterRequest request) {

        Specification<Funcionalidad> spec = Specification
                .where(FuncionalidadSpecification.porEstado(request.estado()))
                .and(FuncionalidadSpecification.porPadreId(request.id_padre()));

        return funcionalidadRepository.findAll(spec).stream().
                map(funcionalidadMapper::toResponse).toList();
    }

    @Override
    public FuncionalidadResponse findById(Long id) {

        Funcionalidad funcionalidad = funcionalidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),"El id no existe"));

        return funcionalidadMapper.toResponse(funcionalidad);

    }

    @Override
    @Transactional
    public FuncionalidadResponse update(Long id, FuncionalidadUpdateRequest request) {

        Funcionalidad funcionalidad = funcionalidadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Funcionalidad con id " + id + " no encontrada"));

        if (request.idPadre() != null) {

            if (request.idPadre().equals(id)) {
                throw new IllegalArgumentException(
                        "Una funcionalidad no puede ser su propio padre");
            }

            Funcionalidad nuevoPadre = funcionalidadRepository.findById(request.idPadre())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Padre con id " + request.idPadre() + " no encontrado"));

            // No puede tener como padre a uno de sus propios descendientes
            if (esDescendiente(funcionalidad, nuevoPadre)) {
                throw new IllegalArgumentException(
                        "No se puede asignar un descendiente como padre (referencia circular)");
            }

            funcionalidad.setPadre(nuevoPadre);

        } else {
            funcionalidad.setPadre(null); // Pasa a ser raíz
        }

        // Actualiza los demás campos
        funcionalidadMapper.applyUpdate(funcionalidad,request);
        Funcionalidad actualizada = funcionalidadRepository.save(funcionalidad);

        return funcionalidadMapper.toResponse(actualizada);

    }

    private boolean esDescendiente(Funcionalidad posibleAncestro, Funcionalidad candidato) {
        Funcionalidad current = candidato.getPadre();

        while (current != null) {
            if (current.getIdFuncionalidad().equals(posibleAncestro.getIdFuncionalidad())) {
                return true; // ciclo detectado
            }
            current = current.getPadre();
        }
        return false;
    }

    private void validaciones(FuncionalidadRequest request){
        List<ApiValidationError> errors = new ArrayList<>();

        if (funcionalidadRepository.existsByNombreFuncionalidad(request.nombreFuncionalidad())) {
            errors.add(new ApiValidationError("nombreFuncionalidad", "Ya existe una funcionalidad con ese nombre"));
        }

        if (!errors.isEmpty()) {
            throw new ConflictException("Campos inválidos en el registro", errors);
        }

    }
}
