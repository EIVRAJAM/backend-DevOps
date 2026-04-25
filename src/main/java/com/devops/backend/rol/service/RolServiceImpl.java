package com.devops.backend.rol.service;

import com.devops.backend.exception.RolDuplicadoException;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.entity.dto.RolFilterRequest;
import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.entity.dto.RolResponse;
import com.devops.backend.rol.mapper.RolMapper;
import com.devops.backend.rol.specification.RolSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import com.devops.backend.rol.repository.RolRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

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
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Rol no encontrado con el id: "+id));

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
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Rol no encontrado con el id: "+id));

        if ("INACTIVO".equals(rol.getEstado())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),"El rol ya se encuentra inactivo");
        }

        rol.setEstado("INACTIVO");
        rolRepository.save(rol);
        return rolMapper.toResponse(rol);
    }
}
