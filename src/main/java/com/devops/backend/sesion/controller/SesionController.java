package com.devops.backend.sesion.controller;

import com.devops.backend.sesion.dto.SesionFilterRequest;
import com.devops.backend.sesion.dto.SesionResponseDto;
import com.devops.backend.sesion.service.SesionService;
import com.devops.backend.usuario.entity.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/sesiones")
public class SesionController {

    private final SesionService sesionService;

    public SesionController(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @GetMapping
    public ResponseEntity<Page<SesionResponseDto>> findAll(
            @RequestParam(required = false, name = "id_usuario") Long idUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) Boolean activa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SesionFilterRequest filter = new SesionFilterRequest(idUsuario, fechaInicio, fechaFin, activa, page, size);
        return ResponseEntity.ok(sesionService.getAllSesiones(filter));
    }

    @GetMapping("/activas")
    public ResponseEntity<Page<SesionResponseDto>> findSesionesActivas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(sesionService.getSesionesActivas(page, size));
    }

    @GetMapping("/ultima")
    public ResponseEntity<SesionResponseDto> findUltimaSesionDelUsuarioActual() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long idUsuario = Long.valueOf(authentication.getName());

        return sesionService.getUltimaSesionByUsuario(idUsuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
