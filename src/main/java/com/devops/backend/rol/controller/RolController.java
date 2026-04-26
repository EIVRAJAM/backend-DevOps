package com.devops.backend.rol.controller;

import com.devops.backend.rol.entity.dto.AsignarFuncionalidadesRequest;
import com.devops.backend.rol.entity.dto.RolFilterRequest;
import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.entity.dto.RolUpdateDto;
import com.devops.backend.rol.service.RolService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/roles")
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
            @RequestParam(required = false)  String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(rolService.findAllFilter(new RolFilterRequest(idRol, nombreRol, estado, page, size)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return ResponseEntity.ok(rolService.findById(id));
    }

    @GetMapping("/nombre({nombre}")
    public ResponseEntity<?> findByName(@PathVariable String nombre){
        return ResponseEntity.ok(rolService.findByName(nombre));
    }
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.desactivarRol(id));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.activarRol(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRol(@PathVariable Long id, @Valid @RequestBody RolUpdateDto request){
        return ResponseEntity.ok(rolService.updateRol(id,request));
    }

    @PutMapping("/{id}/funcionalidades")
    public ResponseEntity<?> asignarFuncionalidades(
            @PathVariable Long id,
            @Valid @RequestBody AsignarFuncionalidadesRequest request) {
        return ResponseEntity.ok(rolService.asignarFuncionalidades(id, request));
    }
    @GetMapping("/{id}/funcionalidades")
    public ResponseEntity<?> findFuncionalidadesByRol(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.findFuncionalidadesByRol(id));
    }

    @DeleteMapping("/{idRol}/funcionalidades/{idFuncionalidad}")
    public ResponseEntity<?> eliminarFuncionalidad(
            @PathVariable Long idRol,
            @PathVariable Long idFuncionalidad) {
        return ResponseEntity.ok(rolService.eliminarFuncionalidad(idRol, idFuncionalidad));
    }

}
