package com.devops.backend.sesion.controller;

import com.devops.backend.sesion.dto.SesionFilterRequest;
import com.devops.backend.sesion.dto.SesionResponseDto;
import com.devops.backend.sesion.service.SesionService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioSesionController {

    private final SesionService sesionService;

    public UsuarioSesionController(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @GetMapping("/{id}/sesiones")
    public ResponseEntity<Page<SesionResponseDto>> findSesionesByUsuario(
            @PathVariable("id") Long idUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SesionFilterRequest filter = new SesionFilterRequest(idUsuario, fechaInicio, fechaFin, null, page, size);
        return ResponseEntity.ok(sesionService.getAllSesiones(filter));
    }
}
