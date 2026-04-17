package com.devops.backend.sesion.service;

import com.devops.backend.sesion.dto.SesionFilterRequest;
import com.devops.backend.sesion.dto.SesionResponseDto;
import com.devops.backend.sesion.entity.Sesion;
import com.devops.backend.sesion.mappers.SesionMapper;
import com.devops.backend.sesion.repository.SesionRepository;
import com.devops.backend.sesion.specification.SesionSpecification;
import com.devops.backend.usuario.repository.UsuarioRepository;
import com.devops.backend.exception.ResourceNotFoundException;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SesionServiceImpl implements SesionService {

    private final SesionRepository sesionRepository;
    private final SesionMapper sesionMapper;
    private final UsuarioRepository usuarioRepository;

    public SesionServiceImpl(SesionRepository sesionRepository, SesionMapper sesionMapper,
            UsuarioRepository usuarioRepository) {
        this.sesionRepository = sesionRepository;
        this.sesionMapper = sesionMapper;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SesionResponseDto> getAllSesiones(SesionFilterRequest filter) {
        int page = Math.max(filter.page(), 0);
        int size = Math.max(filter.size(), 1);

        Specification<Sesion> specification = Specification
                .where(SesionSpecification.porIdUsuario(filter.idUsuario()))
                .and(SesionSpecification.porFechaInicio(filter.fechaInicio()))
                .and(SesionSpecification.porFechaFin(filter.fechaFin()))
                .and(SesionSpecification.porActiva(filter.activa()));

        return sesionRepository.findAll(specification, PageRequest.of(page, size))
                .map(sesionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SesionResponseDto> getSesionesActivas(int page, int size) {
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Specification<Sesion> specification = SesionSpecification.sesionesActivas();

        return sesionRepository.findAll(specification, PageRequest.of(page, size))
                .map(sesionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SesionResponseDto> getUltimaSesionByUsuario(Long idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario con ID " + idUsuario + " no encontrado");
        }
        return sesionRepository.findTopByUsuario_IdUsuarioOrderByFechaInicioDesc(idUsuario)
                .map(sesionMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteSesion(Long idSesion) {
        if (!sesionRepository.existsById(idSesion)) {
            throw new ResourceNotFoundException("Sesión con ID " + idSesion + " no encontrada");
        }
        sesionRepository.deleteById(idSesion);
    }
}
