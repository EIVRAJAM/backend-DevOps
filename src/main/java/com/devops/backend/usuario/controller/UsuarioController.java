package com.devops.backend.usuario.controller;


import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.usuario.dto.*;
import com.devops.backend.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioController {


    private final UsuarioService uService;

    public UsuarioController(UsuarioService uService) {
        this.uService = uService;
    }

    @PostMapping()
    public ResponseEntity<SignUpResponseUsuario> add(@Valid @RequestBody SignUpRequest signUpRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(uService.saveUser(signUpRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<?> findAllNoPage(){
        return ResponseEntity.ok(uService.getAllUsers());
    }

    @GetMapping()
    public ResponseEntity<Page<UserListResponse>> findAll(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String nombreRol,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                uService.getAllUsers(new UsuarioFilterRequest(documento, nombres, apellidos, nombreRol, page, size))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserListResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(uService.findById(id));
    }

    @GetMapping("/document/{document}")   // <-- separado para evitar colisión con /{id}
    public ResponseEntity<UserListResponse> findByDocument(@PathVariable String document) {
        return ResponseEntity.ok(uService.findByDocumento(document));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserListResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequest request) {

        return ResponseEntity.ok(uService.updateUser(id, request));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<UserListResponse> activateUser(@PathVariable Long id) {

        return ResponseEntity.ok(uService.activar(id));
    }
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UserListResponse> deactivateUser(@PathVariable Long id) {

        return ResponseEntity.ok(uService.desactivar(id));
    }
    @PatchMapping("/{id}/bloquear")
    public ResponseEntity<UserListResponse> blockUser(@PathVariable Long id) {

        return ResponseEntity.ok(uService.bloquear(id));
    }
}
