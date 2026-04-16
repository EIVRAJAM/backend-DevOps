package com.devops.backend.usuario.service;

import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.usuario.dto.SignUpResponseUsuario;
import com.devops.backend.usuario.dto.UserListResponse;
import com.devops.backend.usuario.dto.UsuarioDTO;
import com.devops.backend.usuario.dto.UsuarioFilterRequest;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UsuarioService {

    SignUpResponseUsuario saveUser(SignUpRequest signupRequest);
    Usuario save(UsuarioDTO usuarioDTO);

    List<UserListResponse> getAllUsers();
    Page<UserListResponse> getAllUsers(UsuarioFilterRequest dtoFilter);

}