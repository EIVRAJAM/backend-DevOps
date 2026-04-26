package com.devops.backend.rol.service;

import com.devops.backend.rol.enums.Estado;
import com.devops.backend.exception.ApiValidationError;
import com.devops.backend.exception.ConflictException;
import com.devops.backend.exception.RolDuplicadoException;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.entity.dto.RolFilterRequest;
import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.entity.dto.RolResponse;
import com.devops.backend.rol.entity.dto.RolUpdateDto;
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
import java.util.stream.StreamSupport;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final RolMapper rolMapper;

    public RolServiceImpl(RolRepository rolRepository, RolMapper rolMapper) {
        this.rolRepository = rolRepository;
        this.rolMapper = rolMapper;
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

    private Rol findRol(Long id){
        return rolRepository.findById(id).
                orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404)
                        ,"Rol no encontrado con el id: "+id));
    }

    private void validation(RolUpdateDto request){
        List<ApiValidationError> errors = new ArrayList<>();

        if(!rolRepository.findByNombreRol(request.nombreRol()).isEmpty()){
            errors.add(new ApiValidationError("nombreRol", "El nombre ya lo tiene otro rol"));
        }
//        if(request.estado() != Estado.ACTIVO ||
//                request.estado() != Estado.INACTIVO){
//           errors.add(new ApiValidationError("estado", "El rol tiene que ser ACTIVO o INACTIVO"));
//        }

        if (!errors.isEmpty()) {
            throw new ConflictException("Campos duplicados en el registro", errors);
        }
    }

}
