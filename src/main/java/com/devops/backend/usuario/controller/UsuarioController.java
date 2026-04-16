package com.devops.backend.usuario.controller;


import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.usuario.dto.SignUpResponseUsuario;
import com.devops.backend.usuario.dto.UserListResponse;
import com.devops.backend.usuario.dto.UsuarioFilterRequest;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
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


}
