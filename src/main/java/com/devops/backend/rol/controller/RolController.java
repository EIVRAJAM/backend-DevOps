package com.devops.backend.rol.controller;

import com.devops.backend.rol.entity.dto.RolFilterRequest;
import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.service.RolService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/all")
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(rolService.findAll());
    }

    @GetMapping
    public ResponseEntity<?> findAllFilter(
            @RequestParam(required = false) Long idRol,
            @RequestParam(required = false) String nombreRol,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(rolService.findAllFilter(new RolFilterRequest(idRol, nombreRol, estado, page, size)));
    }
}
