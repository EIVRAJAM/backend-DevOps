package com.devops.backend.rol.service;

import com.devops.backend.exception.RolDuplicadoException;
import com.devops.backend.rol.entity.Rol;
import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.entity.dto.RolResponse;
import com.devops.backend.rol.mapper.RolMapper;
import org.springframework.stereotype.Service;

import com.devops.backend.rol.repository.RolRepository;
import org.springframework.transaction.annotation.Transactional;

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
}
