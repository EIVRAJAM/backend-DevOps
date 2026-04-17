package com.devops.backend.sesion.service;

import com.devops.backend.sesion.dto.SesionFilterRequest;
import com.devops.backend.sesion.dto.SesionResponseDto;
import com.devops.backend.sesion.entity.Sesion;
import com.devops.backend.sesion.mappers.SesionMapper;
import com.devops.backend.sesion.repository.SesionRepository;
import com.devops.backend.sesion.specification.SesionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SesionServiceImpl implements SesionService {

    private final SesionRepository sesionRepository;
    private final SesionMapper sesionMapper;

    public SesionServiceImpl(SesionRepository sesionRepository, SesionMapper sesionMapper) {
        this.sesionRepository = sesionRepository;
        this.sesionMapper = sesionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SesionResponseDto> getAllSesiones(SesionFilterRequest filter) {
        int page = Math.max(filter.page(), 0);
        int size = Math.max(filter.size(), 1);

        Specification<Sesion> specification = Specification
                .where(SesionSpecification.porIdUsuario(filter.idUsuario()))
                .and(SesionSpecification.porFechaInicio(filter.fechaInicio()))
                .and(SesionSpecification.porFechaFin(filter.fechaFin()));

        return sesionRepository.findAll(specification, PageRequest.of(page, size))
                .map(sesionMapper::toResponse);
    }
}
