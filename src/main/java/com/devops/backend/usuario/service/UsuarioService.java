package com.devops.backend.usuario.service;

import com.devops.backend.usuario.dto.UsuarioDTO;
import com.devops.backend.usuario.entity.Usuario;

public interface UsuarioService {
    Usuario save(UsuarioDTO usuarioDTO);
}