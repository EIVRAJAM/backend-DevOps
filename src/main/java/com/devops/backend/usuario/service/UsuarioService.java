package com.devops.backend.usuario.service;

import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.auth.dto.SignUpResponse;
import com.devops.backend.usuario.dto.UsuarioDTO;
import com.devops.backend.usuario.entity.Usuario;

public interface UsuarioService {

    Usuario saveUser(SignUpRequest signupRequest);
    Usuario save(UsuarioDTO usuarioDTO);
}