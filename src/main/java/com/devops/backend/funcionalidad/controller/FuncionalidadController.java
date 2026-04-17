package com.devops.backend.funcionalidad.controller;

import com.devops.backend.funcionalidad.dto.FuncionalidadRequest;
import com.devops.backend.funcionalidad.dto.FuncionalidadResponse;
import com.devops.backend.funcionalidad.services.FuncionalidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/funcionalidad")
public class FuncionalidadController {

    private final FuncionalidadService funcionalidadService;

    public FuncionalidadController(FuncionalidadService funcionalidadService) {
        this.funcionalidadService = funcionalidadService;
    }

    @PostMapping
    public ResponseEntity<FuncionalidadResponse> crear(@Valid @RequestBody FuncionalidadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionalidadService.save(request));
    }

}