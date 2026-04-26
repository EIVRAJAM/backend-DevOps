package com.devops.backend.rol.service;

import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.funcionalidad.entity.Funcionalidad;
import com.devops.backend.funcionalidad.repository.FuncionalidadRepository;
import com.devops.backend.rol.entity.dto.*;
import com.devops.backend.rol.enums.Estado;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.exception.RolDuplicadoException;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.mapper.RolMapper;
import com.devops.backend.rol.specification.RolSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import com.devops.backend.rol.repository.RolRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final RolMapper rolMapper;
    private final FuncionalidadRepository funcionalidadRepository;


    public RolServiceImpl(RolRepository rolRepository, RolMapper rolMapper, FuncionalidadRepository funcionalidadRepository) {
        this.rolRepository = rolRepository;
        this.rolMapper = rolMapper;
        this.funcionalidadRepository = funcionalidadRepository;
    }

    @Override
    @Transactional
    public RolResponse save(RolRequest request) {
        if (rolRepository.existsByNombreRolIgnoreCase(request.nombreRol())) {
            throw new RolDuplicadoException(request.nombreRol());
        }

        Rol rol = rolMapper.toEntity(request);
        Rol guardado = rolRepository.save(rol);

        return rolMapper.toResponse(guardado);
    }

    @Override
    public List<RolResponse> findAll() {

        return StreamSupport.stream(rolRepository.findAll().spliterator(),false).
                map(rolMapper::toResponse).toList();

    }

    @Override
    public Page<RolResponse> findAllFilter(RolFilterRequest filter) {

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Specification<Rol> spec = RolSpecification.withFilters(filter);

        return rolRepository.findAll(spec, pageable)
                .map(rolMapper::toResponse);
    }

    @Override
    public RolResponse findById(Long id) {
        Rol rol = findRol(id);
        return rolMapper.toResponse(rol);
    }

    @Override
    public RolResponse findByName(String name) {
        Rol rol = rolRepository.findByNombreRol(name).
                orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),
                "Rol no encontrado con el nombre: "+name));

        return rolMapper.toResponse(rol);
    }

    @Override
    @Transactional
    public RolResponse desactivarRol(Long id) {
        Rol rol = findRol(id);

        if( rol.getEstado() == Estado.INACTIVO) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),"El rol ya se encuentra inactivo");
        }

        rol.setEstado(Estado.INACTIVO);
        rolRepository.save(rol);
        return rolMapper.toResponse(rol);
    }

    @Override
    @Transactional
    public RolResponse activarRol(Long id) {
        Rol rol = findRol(id);

        if (rol.getEstado() == Estado.ACTIVO) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),"El rol ya se encuentra activado");
        }

        rol.setEstado(Estado.ACTIVO);
        rolRepository.save(rol);
        return rolMapper.toResponse(rol);
    }

    @Override
    @Transactional
    public RolResponse updateRol(Long id, RolUpdateDto request) {

        Rol rol = findRol(id);
        validation(request);

        rolMapper.applyUpdate(rol,request);

        Rol rolUpdate = rolRepository.save(rol);

        return rolMapper.toResponse(rolUpdate);
    }

    @Override
    @Transactional
    public RolResponse asignarFuncionalidades(Long idRol, AsignarFuncionalidadesRequest request) {
        Rol rol = findRol(idRol);

        Set<Funcionalidad> funcionalidades = funcionalidadRepository
                .findByIdFuncionalidadIn(request.idsFuncionalidades());

        if (funcionalidades.size() != request.idsFuncionalidades().size()) {
            Set<Long> encontrados = funcionalidades.stream()
                    .map(Funcionalidad::getIdFuncionalidad)
                    .collect(java.util.stream.Collectors.toSet());

            Set<Long> noEncontrados = new java.util.HashSet<>(request.idsFuncionalidades());
            noEncontrados.removeAll(encontrados);

            List<ApiValidationError> errors = noEncontrados.stream()
                    .map(id -> new ApiValidationError("idsFuncionalidades",
                            "No se encontró funcionalidad con id: " + id))
                    .toList();

            throw new ConflictException("Funcionalidades no encontradas", errors);
        }

        // Validar que las funcionalidades estén activas
        List<ApiValidationError> inactivas = funcionalidades.stream()
                .filter(f -> !"ACTIVO".equalsIgnoreCase(f.getEstado()))
                .map(f -> new ApiValidationError("idsFuncionalidades",
                        "La funcionalidad '" + f.getNombreFuncionalidad() + "' no está activa"))
                .toList();

        if (!inactivas.isEmpty()) {
            throw new ConflictException("Funcionalidades inactivas no pueden asignarse", inactivas);
        }

        List<ApiValidationError> duplicados = funcionalidades.stream().
                filter(f->  rol.getFuncionalidades().contains(f)).
                map(f->new ApiValidationError("idFuncionalidad","La funcionalidad: "+
                        f.getNombreFuncionalidad()+" ya se encuentra registrada en el rol")).toList();

        if (!duplicados.isEmpty()) {
            throw new ConflictException("Funcionalidades duplicadas no pueden asignarse", duplicados);
        }

        rol.getFuncionalidades().addAll(funcionalidades);
        Rol rolActualizado = rolRepository.save(rol);

        return rolMapper.toResponse(rolActualizado);
    }

    @Override
    public List<FuncionalidadResponse> findFuncionalidadesByRol(Long idRol) {
        Rol rol = findRol(idRol);

        if (rol.getFuncionalidades() == null || rol.getFuncionalidades().isEmpty()) {
            return List.of();
        }

        return rolMapper.getFuncionalidadByRol(rol);
    }

    @Override
    @Transactional
    public RolResponse eliminarFuncionalidad(Long idRol, Long idFuncionalidad) {

        Rol rol = findRol(idRol);
        Funcionalidad funcionalidad = findFun(idFuncionalidad);

        if (rol.getFuncionalidades() == null || !rol.getFuncionalidades().contains(funcionalidad)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "La funcionalidad '" + funcionalidad.getNombreFuncionalidad() + "' no está asignada a este rol");
        }

        rol.getFuncionalidades().remove(funcionalidad);
        Rol rolActualizado = rolRepository.save(rol);

        return rolMapper.toResponse(rolActualizado);

    }

    private Rol findRol(Long id){
        return rolRepository.findById(id).
                orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404)
                        ,"Rol no encontrado con el id: "+id));
    }

    private Funcionalidad findFun(Long id){
        return funcionalidadRepository.findById(id).
                orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404)
                        ,"Funcion no encontrado con el id: "+id));
    }

    private void validation(RolUpdateDto request){
        List<ApiValidationError> errors = new ArrayList<>();

        if(!rolRepository.findByNombreRol(request.nombreRol()).isEmpty()){
            errors.add(new ApiValidationError("nombreRol", "El nombre ya lo tiene otro rol"));
        }


        if (!errors.isEmpty()) {
            throw new ConflictException("Campos duplicados en el registro", errors);
        }
    }

}
