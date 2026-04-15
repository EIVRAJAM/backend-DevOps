package com.devops.backend.acceso.controller;

import com.devops.backend.acceso.dto.CreateAccesoAdminRequestDTO;
import com.devops.backend.acceso.dto.AccesoAdminDTO;
import com.devops.backend.acceso.entity.Acceso;
import com.devops.backend.acceso.mappers.AccesoMapper;
import com.devops.backend.acceso.service.AccesoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accesos")
public class AccesoController {

    @Autowired
    private AccesoService accesoService;

    @Autowired
    private AccesoMapper accesoMapper;

    @PostMapping
    public ResponseEntity<AccesoAdminDTO> createAcceso(
            @Valid @RequestBody CreateAccesoAdminRequestDTO requestDTO) {

        Acceso acceso = accesoService.saveWithDefaultPassword(
                requestDTO.idUsuario(),
                requestDTO.username(),
                requestDTO.correoAcceso());

        AccesoAdminDTO responseDTO = accesoMapper.toAccesoAdminDTO(acceso);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<Acceso>> getAllAccesos() {
        List<Acceso> accesos = accesoService.findAll();
        return ResponseEntity.ok(accesos);
    }
}
