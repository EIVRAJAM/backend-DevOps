package com.devops.backend.usuario.controller;


import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.usuario.entity.Usuario;
import com.devops.backend.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {


    private final UsuarioService uService;

    public UsuarioController(UsuarioService uService) {
        this.uService = uService;
    }

    @PostMapping()
    public ResponseEntity<Usuario> add(@Valid @RequestBody SignUpRequest signUpRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(uService.saveUser(signUpRequest));
    }
}
