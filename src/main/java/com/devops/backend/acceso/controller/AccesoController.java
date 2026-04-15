package com.devops.backend.acceso.controller;

import com.devops.backend.acceso.dto.CreateAccesoAdminRequestDTO;
import com.devops.backend.acceso.dto.AccesoAdminDTO;
import com.devops.backend.acceso.dto.AccesoUserDTO;
import com.devops.backend.acceso.entity.Acceso;
import com.devops.backend.acceso.mappers.AccesoMapper;
import com.devops.backend.acceso.service.AccesoService;
import com.devops.backend.exception.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accesos")
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

    @GetMapping("/{idUsuario}")
    public ResponseEntity<AccesoAdminDTO> getAccesoByIdUsuario(@PathVariable("idUsuario") Long idUsuario) {
        return accesoService.findByIdUsuario(idUsuario)
                .map(accesoMapper::toAccesoAdminDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<?> updateAcceso(
            @PathVariable("idUsuario") Long idUsuario,
            @Valid @RequestBody AccesoUserDTO requestDTO,
            Authentication authentication) {

        Acceso acceso = accesoService.update(idUsuario, requestDTO);

        // Verificar si el usuario autenticado tiene rol de ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            AccesoAdminDTO responseDTO = accesoMapper.toAccesoAdminDTO(acceso);
            return ResponseEntity.ok(responseDTO);
        } else {
            AccesoUserDTO responseDTO = accesoMapper.toAccesoUserDTO(acceso);
            return ResponseEntity.ok(responseDTO);
        }
    }

    @GetMapping
    public ResponseEntity<List<AccesoAdminDTO>> getAllAccesos() {
        List<AccesoAdminDTO> accesos = accesoService.findAll().stream()
                .map(accesoMapper::toAccesoAdminDTO)
                .toList();
        return ResponseEntity.ok(accesos);
    }
}
