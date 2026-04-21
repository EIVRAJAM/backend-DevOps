package com.devops.backend.rol.controller;

import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.service.RolService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/rol")
@SecurityRequirement(name = "bearerAuth")
public class RolController {

    private RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @PostMapping()
    public ResponseEntity<?> save(@Valid @RequestBody RolRequest request){

        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.save(request));


    }
}
